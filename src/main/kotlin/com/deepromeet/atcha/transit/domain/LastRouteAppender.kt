package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.api.response.LastRoutes
import org.springframework.stereotype.Component

@Component
class LastRouteAppender(
    private val lastRouteCache: LastRouteCache
) {
    fun append(route: LastRoutes) {
        lastRouteCache.cache(route)
    }
}
