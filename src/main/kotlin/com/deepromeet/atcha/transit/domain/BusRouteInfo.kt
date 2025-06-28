package com.deepromeet.atcha.transit.domain

data class BusRouteInfo(
    val route: BusRoute,
    val targetStationIndex: Int,
    val passStopList: BusRouteStationList
) {
    val routeId: String
        get() = route.id.value

    fun getTargetStation(): BusRouteStation {
        return passStopList.busRouteStations[targetStationIndex]
    }
}
