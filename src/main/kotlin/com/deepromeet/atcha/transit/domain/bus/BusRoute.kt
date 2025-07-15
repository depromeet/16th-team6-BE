package com.deepromeet.atcha.transit.domain.bus

data class BusRoute(
    val id: BusRouteId,
    val name: String,
    val serviceRegion: ServiceRegion
)
