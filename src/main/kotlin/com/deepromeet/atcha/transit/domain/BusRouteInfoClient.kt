package com.deepromeet.atcha.transit.domain

interface BusRouteInfoClient {
    fun getBusRoute(routeName: String): List<BusRoute>

    fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo

    fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule?

    fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival
}
