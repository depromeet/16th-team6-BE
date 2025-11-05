package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRoute

interface LastRouteCache {
    fun cache(route: LastRoute)

    fun cacheAll(routes: List<LastRoute>)

    fun get(routeId: String): LastRoute?
}
