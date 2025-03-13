package com.deepromeet.atcha.transit.infrastructure.scheduler

import com.deepromeet.atcha.transit.infrastructure.cache.LastRouteRedisCache
import com.deepromeet.atcha.transit.infrastructure.refresher.RouteDepartureTimeRefresher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class LastRouteScheduler(
    private val lastRouteRedisCache: LastRouteRedisCache,
    private val routeDepartureTimeRefresher: RouteDepartureTimeRefresher
) {
    @Scheduled(fixedRate = 60000)
    fun refreshDepartureTime() {
        lastRouteRedisCache.processRoutes { route ->
            routeDepartureTimeRefresher.refreshDepartureTime(route)
        }
    }
}
