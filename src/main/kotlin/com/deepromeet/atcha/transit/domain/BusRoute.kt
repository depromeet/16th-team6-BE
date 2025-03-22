package com.deepromeet.atcha.transit.domain

data class BusRoute(
    val id: BusRouteId,
    val name: String,
    val serviceRegion: ServiceRegion
)
