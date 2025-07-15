package com.deepromeet.atcha.transit.domain.route

import com.deepromeet.atcha.location.domain.Coordinate
import org.springframework.stereotype.Component

@Component
class LastRouteAppender(
    private val lastRouteCache: LastRouteCache,
    private val lastRouteIndexCache: LastRouteIndexCache
) {
    fun append(route: LastRoute) {
        lastRouteCache.cache(route)
    }

    fun appendRoutes(
        start: Coordinate,
        end: Coordinate,
        routes: List<LastRoute>
    ) {
        lastRouteIndexCache.cache(start, end, routes.map { it.id })
        routes.forEach { route ->
            lastRouteCache.cache(route)
        }
    }
}
