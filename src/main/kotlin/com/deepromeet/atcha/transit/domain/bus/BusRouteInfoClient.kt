package com.deepromeet.atcha.transit.domain.bus

interface BusRouteInfoClient {
    companion object {
        val NON_STOP_STATION_NAME = listOf("(미정차)", "(경유)")
    }

    fun isValidStation(station: BusRouteStation): Boolean =
        NON_STOP_STATION_NAME.none { keyword ->
            fun isNotGarage(station: BusRouteStation): Boolean = station.order != 1
            station.stationName.contains(keyword) && isNotGarage(station)
        }

    suspend fun getBusRoute(routeName: String): List<BusRoute>

    suspend fun getStationList(route: BusRoute): BusRouteStationList

    suspend fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo

    suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule

    suspend fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrival
}
