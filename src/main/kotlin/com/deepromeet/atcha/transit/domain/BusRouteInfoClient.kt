package com.deepromeet.atcha.transit.domain

interface BusRouteInfoClient {
    fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival?

    fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo?
}
