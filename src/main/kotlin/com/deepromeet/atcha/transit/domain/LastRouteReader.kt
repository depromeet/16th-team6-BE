package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import com.deepromeet.atcha.transit.exception.TransitException
import org.springframework.stereotype.Component

@Component
class LastRouteReader(
    private val lastRouteCache: LastRouteCache
) {
    fun read(routeId: String): LastRoutesResponse {
        return lastRouteCache.get(routeId) ?: throw TransitException.NotFoundRoute
    }

    fun readRemainingTime(routeId: String): Int = read(routeId).getRemainingTime()
}
