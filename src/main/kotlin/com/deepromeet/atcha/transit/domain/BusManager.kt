package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.odsay.ODSayBusInfoClient
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
class BusManager(
    private val oDSayBusInfoClient: ODSayBusInfoClient,
    private val busStationInfoClientMap: Map<ServiceRegion, BusStationInfoClient>,
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>,
    private val regionIdentifier: RegionIdentifier,
    private val busTimeTableCache: BusTimeTableCache
) {
    fun getSchedule(
        routeName: String,
        meta: BusStationMeta
    ): BusSchedule {
        val (station, route) = findStationAndRoute(routeName, meta)

        val schedule =
            busRouteInfoClientMap[route.serviceRegion]?.getBusSchedule(station, route)
                ?: oDSayBusInfoClient.getBusSchedule(station, route)
                ?: throw TransitException.NotFoundBusArrival

        busTimeTableCache.cache(routeName, meta, schedule.busTimeTable)
        return schedule
    }

    fun getRealTimeArrival(
        routeName: String,
        meta: BusStationMeta
    ): BusRealTimeArrival {
        val (station, route) = findStationAndRoute(routeName, meta)

        return busRouteInfoClientMap[route.serviceRegion]
            ?.getBusRealTimeInfo(station, route)
            ?: throw TransitException.NotFoundBusRealTime
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
            ?: throw TransitException.BusRouteOperationInfoFetchFailed
    }

    suspend fun getBusPositions(route: BusRoute): BusRoutePositions =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val stations = async { busStationInfoClientMap[route.serviceRegion]!!.getByRoute(route) }
                val positions = async { busPositionFetcherMap[route.serviceRegion]!!.fetch(route.id) }
                BusRoutePositions(
                    stations.await() ?: throw TransitException.BusRouteStationListFetchFailed,
                    positions.await()
                )
            }
        }

    private fun findStationAndRoute(
        routeName: String,
        meta: BusStationMeta
    ): Pair<BusStation, BusRoute> {
        val region = regionIdentifier.identify(meta.coordinate)

        val station =
            busStationInfoClientMap[region]
                ?.getStationByName(meta)
                ?: run {
                    log.warn { "$region - $meta 정류장 정보 실패" }
                    throw TransitException.NotFoundBusStation
                }

        val route =
            busStationInfoClientMap[region]
                ?.getRoute(station, routeName)
                ?: run {
                    log.warn { "$region - 버스 노선($routeName) 정보 실패" }
                    throw TransitException.NotFoundBusRoute
                }

        return station to route
    }
}
