package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.api.response.LastRoutesResponse

interface LastRouteCache {
    fun processRoutes(action: (LastRoutesResponse) -> Unit)

    fun cache(route: LastRoutesResponse)

    fun get(routeId: String): LastRoutesResponse?
}
