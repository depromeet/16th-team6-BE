package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.LastRoute
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class LastRouteAppender(
    private val lastRouteCache: LastRouteCache,
    private val lastRouteIndexCache: LastRouteIndexCache,
    routeCache: LastRouteCache
) {
    fun append(route: LastRoute) {
        lastRouteCache.cache(route)
    }

    fun appendRoutes(
        start: Coordinate,
        end: Coordinate,
        routes: List<LastRoute>
    ) {
        val lastDepartureTime = routes.maxBy { it.departureDateTime }.departureDateTime
        val ttl = Duration.between(LocalDateTime.now(), lastDepartureTime)
        lastRouteIndexCache.cache(start, end, routes.map { it.id }, ttl)
        lastRouteCache.cacheAll(routes)
    }
}
