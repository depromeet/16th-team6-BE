package com.deepromeet.atcha.transit.domain.bus

data class BusRouteInfo(
    val route: BusRoute,
    val targetStation: BusRouteStation,
    val passStopList: BusRouteStationList
) {
    val routeId: String
        get() = route.id.value
}
