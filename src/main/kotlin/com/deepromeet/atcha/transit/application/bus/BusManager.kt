package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.bus.BusRoutePositions
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.region.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.PassStopList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class BusManager(
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>,
    private val busScheduleProvider: BusScheduleProvider,
    private val busRouteResolver: BusRouteResolver,
    private val busTimeTableCache: BusTimeTableCache,
    private val startedBusCache: StartedBusCache
) {
    suspend fun getSchedule(
        routeName: String,
        stationMeta: BusStationMeta,
        passStops: PassStopList
    ): BusSchedule {
        busTimeTableCache.get(routeName, stationMeta)?.let { return it }
        val busRouteInfo = busRouteResolver.resolve(routeName, stationMeta, passStops)

        val schedule =
            busScheduleProvider.getBusSchedule(busRouteInfo)
                ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_SCHEDULE,
                    "버스 노선 '$routeName' 정류소 '${stationMeta.name}'의 시간표 정보를 찾을 수 없습니다."
                )

        busTimeTableCache.cache(routeName, stationMeta, schedule)
        return schedule
    }

    suspend fun getRealTimeArrival(
        routeName: String,
        meta: BusStationMeta,
        passStopList: PassStopList
    ): BusRealTimeArrival {
        val routeInfo = busRouteResolver.resolve(routeName, meta, passStopList)
        return busRouteInfoClientMap[routeInfo.route.serviceRegion]!!.getBusRealTimeInfo(routeInfo)
    }

    suspend fun getBusRouteOperationInfo(route: BusRoute): BusRouteOperationInfo {
        return busRouteInfoClientMap[route.serviceRegion]!!.getBusRouteInfo(route)
    }

    suspend fun getBusPositions(route: BusRoute): BusRoutePositions =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val stations = async { busRouteInfoClientMap[route.serviceRegion]!!.getStationList(route) }
                val positions = async { busPositionFetcherMap[route.serviceRegion]!!.fetch(route.id) }
                BusRoutePositions(stations.await(), positions.await())
            }
        }

    suspend fun isBusStarted(lastRoute: LastRoute): Boolean {
        val firstBus = lastRoute.findFirstBus()
        val busInfo = firstBus.transitInfo as TransitInfo.BusInfo

        val busPositions =
            runCatching {
                getBusPositions(busInfo.busRoute)
            }.getOrElse { return false }

        busPositions.findTargetBus(
            busInfo.busStation,
            firstBus.departureDateTime!!,
            busInfo.timeTable.term
        )?.let {
            startedBusCache.cache(lastRoute.id, it)
            return true
        }

        return false
    }
}
