package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate

interface LastRouteIndexCache {
    fun cache(
        start: Coordinate,
        end: Coordinate,
        routeIds: List<String>
    )

    fun get(
        start: Coordinate,
        end: Coordinate
    ): List<String>
}
