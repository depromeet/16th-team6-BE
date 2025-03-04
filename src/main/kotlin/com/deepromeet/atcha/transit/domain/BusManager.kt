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
        stationInfo: StationInfo
    ): BusArrival {
        val station =
            busStationInfoClient.getStationByName(stationInfo)
                ?: throw TransitException.NotFoundBusStation
        val busRoute =
            busStationInfoClient.getRoute(station, routeName)
                ?: throw TransitException.NotFoundBusRoute
        return busArrivalInfoFetcher.getBusArrival(station, busRoute)
            ?: throw TransitException.NotFoundBusArrival
    }
}
