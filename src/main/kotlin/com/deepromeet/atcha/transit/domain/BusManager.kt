package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
class BusManager(
    private val busStationInfoClientMap: Map<ServiceRegion, BusStationInfoClient>,
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>,
    private val busScheduleProvider: BusScheduleProvider,
    private val regionIdentifier: RegionIdentifier,
    private val busTimeTableCache: BusTimeTableCache
) {
    fun getSchedule(
        routeName: String,
        meta: BusStationMeta
    ): BusSchedule {
        val (station, route) = findStationAndRoute(routeName, meta)

        val schedule =
            busScheduleProvider.getBusSchedule(station, route)
                ?: throw TransitException.of(
                    TransitError.NOT_FOUND_BUS_SCHEDULE,
                    "버스 노선 '$routeName' 정류소 '${meta.name}'의 도착 정보를 찾을 수 없습니다."
                )

        busTimeTableCache.cache(routeName, meta, schedule.busTimeTable)
        return schedule
    }

    fun getRealTimeArrival(
        routeName: String,
        meta: BusStationMeta
    ): BusRealTimeArrival {
        val (station, route) = findStationAndRoute(routeName, meta)
        return busRouteInfoClientMap[route.serviceRegion]!!.getBusRealTimeInfo(station, route)
    }

    fun getBusTimeInfo(
        routeName: String,
        stationMeta: BusStationMeta
    ): BusTimeTable {
        return busTimeTableCache.get(
            routeName,
            stationMeta
        ) ?: getSchedule(routeName, stationMeta).busTimeTable
    }

    fun getBusRouteOperationInfo(route: BusRoute): BusRouteOperationInfo {
        return busRouteInfoClientMap[route.serviceRegion]!!.getBusRouteInfo(route)
    }

    suspend fun getBusPositions(route: BusRoute): BusRoutePositions =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val stations = async { busStationInfoClientMap[route.serviceRegion]!!.getByRoute(route) }
                val positions = async { busPositionFetcherMap[route.serviceRegion]!!.fetch(route.id) }
                BusRoutePositions(stations.await(), positions.await())
            }
        }

    private fun findStationAndRoute(
        routeName: String,
        meta: BusStationMeta
    ): Pair<BusStation, BusRoute> {
        val region = regionIdentifier.identify(meta.coordinate)
        val station = busStationInfoClientMap[region]!!.getStationByName(meta)
        val route = busStationInfoClientMap[region]!!.getRoute(station, routeName)
        return station to route
    }
}
