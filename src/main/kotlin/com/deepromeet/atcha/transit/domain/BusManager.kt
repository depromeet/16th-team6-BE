package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

@Component
class BusManager(
    private val busStationInfoClient: BusStationInfoClient,
    private val busArrivalInfoFetcher: BusArrivalInfoFetcher
) {
    fun getArrivalInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival {
        val station =
            busStationInfoClient.getStationByName(busStationMeta)
                ?: throw TransitException.NotFoundBusStation
        val busRoute =
            busStationInfoClient.getRoute(station, routeName)
                ?: throw TransitException.NotFoundBusRoute
        return busArrivalInfoFetcher.getBusArrival(station, busRoute)
            ?: throw TransitException.NotFoundBusArrival
    }
}
