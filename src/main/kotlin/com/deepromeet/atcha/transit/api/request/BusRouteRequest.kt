package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.ServiceRegion

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
