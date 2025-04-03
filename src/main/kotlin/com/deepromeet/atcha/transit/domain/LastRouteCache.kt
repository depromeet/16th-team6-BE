package com.deepromeet.atcha.transit.domain

interface LastRouteCache {
    fun cache(route: LastRoutes)

    fun get(routeId: String): LastRoutes?
}
