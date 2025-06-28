package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
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
    private val regionIdentifier: RegionIdentifier,
    private val busTimeTableCache: BusTimeTableCache,
    private val busRouteMatcher: BusRouteMatcher
) {
    fun getBusTimeInfo(
        routeName: String,
        stationMeta: BusStationMeta,
        nextStationName: String?
    ): BusTimeTable {
        return busTimeTableCache.get(
            routeName,
            stationMeta
        ) ?: getSchedule(routeName, stationMeta, nextStationName).busTimeTable
    }

    fun getSchedule(
        routeName: String,
        stationMeta: BusStationMeta,
        nextStationName: String?
    ): BusSchedule {
        val busRouteInfo = getBusRouteInfo(routeName, stationMeta, nextStationName)

        val schedule =
            busScheduleProvider.getBusSchedule(busRouteInfo)
                ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_SCHEDULE,
                    "버스 노선 '$routeName' 정류소 '${stationMeta.name}'의 도착 정보를 찾을 수 없습니다."
                )

        busTimeTableCache.cache(routeName, stationMeta, schedule.busTimeTable)
        return schedule
    }

    fun getRealTimeArrival(
        routeName: String,
        meta: BusStationMeta,
        nextStationName: String?
    ): BusRealTimeArrival {
        val routeInfo = getBusRouteInfo(routeName, meta, nextStationName)
        return busRouteInfoClientMap[routeInfo.route.serviceRegion]!!.getBusRealTimeInfo(routeInfo)
    }

    fun getBusRouteOperationInfo(route: BusRoute): BusRouteOperationInfo {
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

    private fun getBusRouteInfo(
        routeName: String,
        meta: BusStationMeta,
        nextStationName: String?
    ): BusRouteInfo {
        val region = regionIdentifier.identify(meta.coordinate)
        val busRoutes = busRouteInfoClientMap[region]!!.getBusRoute(routeName)
        return busRouteMatcher.getMatchedRoute(busRoutes, meta, nextStationName)
    }
}
