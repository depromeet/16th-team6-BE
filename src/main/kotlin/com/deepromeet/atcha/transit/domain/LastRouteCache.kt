package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.api.response.LastRoutes

interface LastRouteCache {
    fun cache(route: LastRoutes)

    fun get(routeId: String): LastRoutes?
}
