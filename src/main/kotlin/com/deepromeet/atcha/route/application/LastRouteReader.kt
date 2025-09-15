package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.sort
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
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
        return lastRouteCache.get(routeId)
            ?: throw TransitException.of(
                TransitError.NOT_FOUND_ROUTE,
                "경로 ID '$routeId'에 해당하는 경로를 찾을 수 없습니다."
            )
    }

    suspend fun read(
        start: Coordinate,
        end: Coordinate
    ): List<LastRoute>? =
        coroutineScope {
            lastRouteIndexCache.get(start, end).takeIf { it.isNotEmpty() }?.map {
                async { read(it) }
            }?.awaitAll()?.sort()
        }

    fun readRemainingTime(routeId: String): Int = read(routeId).calculateRemainingTime()
}
