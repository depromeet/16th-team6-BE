package com.deepromeet.atcha.transit.domain

interface BusRouteInfoClient {
    companion object {
        val NON_STOP_STATION_NAME = listOf("(미정차)", "(경유)")
    }

    fun getBusRoute(routeName: String): List<BusRoute>

    fun getStationList(route: BusRoute): BusRouteStationList

    fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo

    fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule?

    fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival
}
