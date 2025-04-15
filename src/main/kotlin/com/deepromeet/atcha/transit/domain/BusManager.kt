package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
class BusManager(
    private val busStationInfoClientMap: Map<ServiceRegion, BusStationInfoClient>,
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>,
    private val regionIdentifier: RegionIdentifier,
    private val busTimeTableCache: BusTimeTableCache
) {
    fun getArrivalInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival? {
        val region = regionIdentifier.identify(busStationMeta.coordinate)
        val station =
            busStationInfoClientMap[region]?.getStationByName(busStationMeta)
                .logIfNull(
                    "[NotFoundBusStation] region=$region," +
                        " station=${busStationMeta.resolveName()}"
                )
                ?: return null
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                .logIfNull(
                    "[NotFoundBusRoute] region=$region," +
                        " station=${station.busStationMeta.name}, routeName=$routeName"
                )
                ?: return null
        val busArrival =
            busRouteInfoClientMap[region]?.getBusArrival(station, busRoute)
                .logIfNull(
                    "[NotFoundBusArrival] region=$region, " +
                        "station=${station.busStationMeta.name}, routeName=$routeName"
                )
        if (busArrival != null) {
            busTimeTableCache.cache(routeName, busStationMeta, busArrival.busTimeTable)
        }

        return busArrival
    }

    fun getBusTimeInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusTimeTable? {
        return busTimeTableCache.get(routeName, busStationMeta)
            ?: getArrivalInfo(routeName, busStationMeta)?.busTimeTable
    }

    fun getBusRouteOperationInfo(route: BusRoute): BusRouteOperationInfo {
        return busRouteInfoClientMap[route.serviceRegion]!!.getBusRouteInfo(route)
            ?: throw TransitException.BusRouteOperationInfoFetchFailed
    }

    suspend fun getBusPositions(busRoute: BusRoute): BusRoutePositions =
        coroutineScope {
            val stationListDeferred =
                async(Dispatchers.IO) {
                    busStationInfoClientMap[busRoute.serviceRegion]!!
                        .getByRoute(busRoute)
                        ?: throw TransitException.BusRouteStationListFetchFailed
                }

            val positionsDeferred =
                async(Dispatchers.IO) {
                    busPositionFetcherMap[busRoute.serviceRegion]!!
                        .fetch(busRoute.id)
                }

            BusRoutePositions(stationListDeferred.await(), positionsDeferred.await())
        }
}

fun <T> T?.logIfNull(message: String): T? {
    if (this == null) {
        log.warn { message }
    }
    return this
}
