package com.deepromeet.atcha.transit.domain

data class BusRouteDetail(
    val routeStations: BusRouteStationList,
    val busPositions: List<BusPosition>
)
