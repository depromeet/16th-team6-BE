package com.deepromeet.atcha.transit.domain

interface BusRouteInfoClient {
    fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo?

    fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival?
}
