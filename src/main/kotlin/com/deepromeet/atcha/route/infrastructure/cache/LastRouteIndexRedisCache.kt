package com.deepromeet.atcha.route.infrastructure.cache

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.application.LastRouteIndexCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LastRouteIndexRedisCache(
    private val lastRouteIndexRedisTemplate: RedisTemplate<String, String>
) : LastRouteIndexCache {
    override fun cache(
        start: Coordinate,
        end: Coordinate,
        routeIds: List<String>
    ) {
        val key = getKey(start, end)
        routeIds.forEach {
            lastRouteIndexRedisTemplate.opsForList().rightPush(key, it)
        }
        lastRouteIndexRedisTemplate.expire(key, 12, TimeUnit.HOURS)
    }

    override fun get(
        start: Coordinate,
        end: Coordinate
    ): List<String> {
        return lastRouteIndexRedisTemplate.opsForList().range(getKey(start, end), 0, -1) ?: emptyList()
    }

    fun getKey(
        start: Coordinate,
        end: Coordinate
    ): String {
        return "routes:coordinate:${start.lon},${start.lat}:${end.lon},${end.lat}"
    }
}
