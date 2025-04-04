package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.exception.TransitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class LastRouteReader(
    private val lastRouteCache: LastRouteCache,
    private val lastRouteIndexCache: LastRouteIndexCache
) {
    fun read(routeId: String): LastRoute {
        return lastRouteCache.get(routeId) ?: throw TransitException.NotFoundRoute
    }

    suspend fun read(
        start: Coordinate,
        end: Coordinate
    ): List<LastRoute>? =
        coroutineScope {
            val routeIds = lastRouteIndexCache.get(start, end)
            return@coroutineScope if (routeIds.isNotEmpty()) {
                val routes =
                    routeIds
                        .map { id -> async(Dispatchers.IO) { read(id) } }
                        .awaitAll()
                routes
            } else {
                null
            }
        }

    fun readRemainingTime(routeId: String): Int = read(routeId).calculateRemainingTime()
}
