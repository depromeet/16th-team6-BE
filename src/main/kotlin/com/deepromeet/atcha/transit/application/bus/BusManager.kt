package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrivals
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.bus.BusRoutePositions
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BusManager(
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>,
    private val busScheduleProvider: BusScheduleProvider,
    private val busRouteResolver: BusRouteResolver,
    private val busTimeTableCache: BusTimeTableCache
) {
    suspend fun getSchedule(
        routeName: String,
        stationMeta: BusStationMeta,
        passStops: RoutePassStops
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
        passStopList: RoutePassStops
    ): BusRealTimeArrivals {
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

    suspend fun locateBus(
        busInfo: TransitInfo.BusInfo,
        departureDateTime: LocalDateTime
    ): BusPosition? {
        val busPositions =
            runCatching {
                getBusPositions(busInfo.busRouteInfo.route)
            }.getOrNull() ?: return null

        return busPositions.findTargetBus(
            busInfo.busStation,
            departureDateTime,
            busInfo.timeTable.term
        )
    }
}
