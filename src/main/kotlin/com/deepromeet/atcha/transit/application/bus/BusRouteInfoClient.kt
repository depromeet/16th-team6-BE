package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrivals
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteStation
import com.deepromeet.atcha.transit.domain.bus.BusRouteStationList
import com.deepromeet.atcha.transit.domain.bus.BusSchedule

interface BusRouteInfoClient {
    companion object {
        val NON_STOP_STATION_NAME = listOf("(미정차)", "(경유)")

        fun isValidStation(station: BusRouteStation): Boolean =
            NON_STOP_STATION_NAME.none { keyword ->
                fun isNotGarage(station: BusRouteStation): Boolean = station.order != 1
                station.stationName.contains(keyword) && isNotGarage(station)
            }
    }

    suspend fun getBusRoute(routeName: String): List<BusRoute>

    suspend fun getStationList(route: BusRoute): BusRouteStationList

    suspend fun getBusRouteInfo(route: BusRoute): BusRouteOperationInfo

    suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule

    suspend fun getBusRealTimeInfo(routeInfo: BusRouteInfo): BusRealTimeArrivals
}
