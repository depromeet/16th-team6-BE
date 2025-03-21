package com.deepromeet.atcha.transit.domain

data class BusRoutePositions(
    val routeStations: BusRouteStationList,
    val busPositions: List<BusPosition>
)
