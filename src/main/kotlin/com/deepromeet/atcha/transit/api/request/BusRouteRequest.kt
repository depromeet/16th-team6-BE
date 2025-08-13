package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteId

data class BusRouteRequest(
    val busRouteId: String,
    val routeName: String,
    val serviceRegion: ServiceRegion
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(busRouteId),
            name = routeName,
            serviceRegion = serviceRegion
        )
    }
}
