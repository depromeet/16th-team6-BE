package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

@Component
class LastRouteReader(
    private val lastRouteCache: LastRouteCache
) {
    fun read(routeId: String): LastRoutes {
        return lastRouteCache.get(routeId) ?: throw TransitException.NotFoundRoute
    }

    fun readRemainingTime(routeId: String): Int = read(routeId).calculateRemainingTime()
}
