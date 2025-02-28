package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.repository.BusRouteRepository
import com.deepromeet.atcha.transit.infrastructure.repository.BusStationRepository
import org.springframework.stereotype.Component

@Component
class BusManager(
    private val busRouteRepository: BusRouteRepository,
    private val busStationRepository: BusStationRepository,
    private val busArrivalInfoFetcher: BusArrivalInfoFetcher
) {
    fun getArrivalInfo(
        routeName: String,
        stationName: String
    ): BusArrival {
        val busRoute = getBusRoute(routeName)
        val busStation = getBusStation(busRoute, stationName) // JOIN으로 묶어서 한번에 가져오는게 좋을듯
        return busArrivalInfoFetcher.getBusArrival(busRoute.routeId, busStation.stationId, busStation.order)
    }

    fun getBusRoute(routeName: String): BusRoute {
        return busRouteRepository.findByRouteName(routeName)
            ?: throw TransitException.NotFoundBusRoute
    }

    fun getBusStation(
        busRoute: BusRoute,
        stationName: String
    ): BusStation {
        return busStationRepository.findByRouteIdAndStationName(busRoute.routeId, stationName)
            ?: throw TransitException.NotFoundBusStation
    }
}
