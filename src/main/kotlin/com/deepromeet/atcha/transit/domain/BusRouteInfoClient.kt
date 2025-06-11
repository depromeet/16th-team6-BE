package com.deepromeet.atcha.transit.domain

interface BusRouteInfoClient {
    fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule?

    fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo?

    fun getBusRealTimeInfo(
        station: BusStation,
        route: BusRoute
    ): BusRealTimeArrival?
}
