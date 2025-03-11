package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

@Component
class BusManager(
    private val busStationInfoClientMap: Map<BusRegion, BusStationInfoClient>,
    private val busArrivalInfoFetcherMap: Map<BusRegion, BusArrivalInfoFetcher>,
    private val regionIdentifier: RegionIdentifier
) {
    fun getArrivalInfo(
        routeName: String,
        busStationMeta: BusStationMeta
    ): BusArrival {
        val region = regionIdentifier.identify(busStationMeta.coordinate)
        val station =
            busStationInfoClientMap[region]?.getStationByName(busStationMeta)
                ?: throw TransitException.NotFoundBusStation
        val busRoute =
            busStationInfoClientMap[region]?.getRoute(station, routeName)
                ?: throw TransitException.NotFoundBusRoute
        return busArrivalInfoFetcherMap[region]?.getBusArrival(station, busRoute)
            ?: throw TransitException.NotFoundBusArrival
    }
}
