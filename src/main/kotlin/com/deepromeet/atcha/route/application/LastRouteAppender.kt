package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.infrastructure.cache.LastRouteMetricsRepository
import org.springframework.stereotype.Component

@Component
class LastRouteAppender(
    private val lastRouteCache: LastRouteCache,
    private val lastRouteIndexCache: LastRouteIndexCache,
    private val metricsRepository: LastRouteMetricsRepository
) {
    fun append(route: LastRoute) {
        lastRouteCache.cache(route)
    }

    fun appendRoutes(
        start: Coordinate,
        end: Coordinate,
        routes: List<LastRoute>
    ) {
        metricsRepository.incrSuccess(routes.size.toLong())
        lastRouteIndexCache.cache(start, end, routes.map { it.id })
        routes.forEach { route ->
            lastRouteCache.cache(route)
        }
    }
}
