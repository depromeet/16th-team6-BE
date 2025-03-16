package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import org.springframework.stereotype.Component

@Component
class LastRouteIndexReader(
    private val lastRouteIndexCache: LastRouteIndexCache
) {
    fun read(
        start: Coordinate,
        end: Coordinate
    ): List<String> {
        return lastRouteIndexCache.get(start, end)
    }
}
