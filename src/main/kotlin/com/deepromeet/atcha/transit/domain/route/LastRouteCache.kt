package com.deepromeet.atcha.transit.domain.route

interface LastRouteCache {
    fun cache(route: LastRoute)

    fun get(routeId: String): LastRoute?
}
