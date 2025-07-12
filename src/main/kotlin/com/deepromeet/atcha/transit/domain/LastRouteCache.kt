package com.deepromeet.atcha.transit.domain

interface LastRouteCache {
    fun cache(route: LastRoute)

    fun get(routeId: String): LastRoute?
}
