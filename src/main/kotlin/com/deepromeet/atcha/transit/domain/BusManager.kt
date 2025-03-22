package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
class BusManager(
    private val busStationInfoClientMap: Map<ServiceRegion, BusStationInfoClient>,
    private val busRouteInfoClientMap: Map<ServiceRegion, BusRouteInfoClient>,
    private val busPositionFetcherMap: Map<ServiceRegion, BusPositionFetcher>,
    private val regionIdentifier: RegionIdentifier
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
                        " station=${busStationMeta.name}"
                )
                ?: return null
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                .logIfNull(
                    "[NotFoundBusRoute] region=$region," +
                        " station=${station.busStationMeta.name}, routeName=$routeName"
                )
                ?: return null
        return busRouteInfoClientMap[region]?.getBusArrival(station, busRoute)
            .logIfNull(
                "[NotFoundBusArrival] region=$region, " +
                    "station=${station.busStationMeta.name}, routeName=$routeName"
            )
    }

    fun getBusRouteOperationInfo(route: BusRoute): BusRouteOperationInfo {
        return busRouteInfoClientMap[route.serviceRegion]!!.getBusRouteInfo(route)
            ?: throw TransitException.BusRouteOperationInfoFetchFailed
    }

    fun getBusRouteStationList(busRoute: BusRoute): BusRouteStationList {
        return busStationInfoClientMap[busRoute.serviceRegion]!!.getByRoute(busRoute)
            ?: throw TransitException.BusRouteStationListFetchFailed
    }

    fun getBusPosition(busRoute: BusRoute): List<BusPosition> {
        return busPositionFetcherMap[busRoute.serviceRegion]!!.fetch(busRoute.id)
    }
}

fun <T> T?.logIfNull(message: String): T? {
    if (this == null) {
        log.warn { message }
    }
    return this
}
