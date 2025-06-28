package com.deepromeet.atcha.transit.domain

data class BusRouteStationList(
    val busRouteStations: List<BusRouteStation>,
    val turnPoint: Int?
)
