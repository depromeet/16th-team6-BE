package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import org.springframework.stereotype.Component

@Component
class LastRouteIndexAppender(
    private val lastRouteIndexCache: LastRouteIndexCache
) {
    fun append(
        start: Coordinate,
        end: Coordinate,
        routeIds: List<String>
    ) {
        lastRouteIndexCache.cache(start, end, routeIds)
    }
}
