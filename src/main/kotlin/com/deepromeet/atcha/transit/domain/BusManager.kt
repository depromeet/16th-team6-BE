package com.deepromeet.atcha.transit.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

val log = KotlinLogging.logger {}

@Component
class BusManager(
    private val busStationInfoClientMap: Map<ServiceRegion, BusStationInfoClient>,
    private val busArrivalInfoFetcherMap: Map<ServiceRegion, BusArrivalInfoFetcher>,
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
        return busArrivalInfoFetcherMap[region]?.getBusArrival(station, busRoute)
            .logIfNull(
                "[NotFoundBusArrival] region=$region, " +
                    "station=${station.busStationMeta.name}, routeName=$routeName"
            )
    }
}

fun <T> T?.logIfNull(message: String): T? {
    if (this == null) {
        log.warn { message }
    }
    return this
}
