package com.deepromeet.atcha.transit.domain

data class BusStation(
    val routeId: RouteId,
    val stationId: StationId,
    val stationName: String,
    val order: Int
)
