package com.deepromeet.atcha.transit.domain

interface BusRouteInfoClient {

    fun getBusRouteInfo(
        route: BusRoute
    ): BusRouteInfo?

    fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival?

}
