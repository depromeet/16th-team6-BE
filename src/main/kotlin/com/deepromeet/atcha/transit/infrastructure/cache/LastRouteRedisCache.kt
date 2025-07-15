package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.domain.route.LastRoute
import com.deepromeet.atcha.transit.domain.route.LastRouteCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LastRouteRedisCache(
    private val lastRouteRedisTemplate: RedisTemplate<String, LastRoute>
) : LastRouteCache {
    override fun get(routeId: String): LastRoute? {
        return lastRouteRedisTemplate.opsForValue().get(getKey(routeId))
    }

    override fun cache(route: LastRoute) {
        lastRouteRedisTemplate.opsForValue().set(getKey(route.routeId), route, Duration.ofHours(12))
    }

    fun getKey(routeId: String): String {
        return "routes:last:$routeId"
    }
}
