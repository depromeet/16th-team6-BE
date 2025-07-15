package com.deepromeet.atcha.transit.domain.bus

import com.deepromeet.atcha.transit.domain.region.ServiceRegion

data class BusRoute(
    val id: BusRouteId,
    val name: String,
    val serviceRegion: ServiceRegion
)
