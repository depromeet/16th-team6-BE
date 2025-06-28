package com.deepromeet.atcha.transit.domain

interface BusStationInfoClient {
    companion object {
        val NON_STOP_STATION_NAME = listOf("(미정차)", "(경유)")
    }

    fun getStationByName(info: BusStationMeta): BusStation

    fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute

    fun getByRoute(route: BusRoute): BusRouteStationList
}
