package com.deepromeet.atcha.route.infrastructure.cache

import com.deepromeet.atcha.route.application.LastRouteCache
import com.deepromeet.atcha.route.domain.LastRoute
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
        lastRouteRedisTemplate.opsForValue().set(getKey(route.id), route, Duration.ofDays(1))
    }

    fun getKey(routeId: String): String {
        return "routes:last:$routeId"
    }
}
