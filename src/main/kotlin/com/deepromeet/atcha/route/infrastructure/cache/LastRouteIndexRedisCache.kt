package com.deepromeet.atcha.route.infrastructure.cache

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.application.LastRouteIndexCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LastRouteIndexRedisCache(
    private val lastRouteIndexRedisTemplate: RedisTemplate<String, String>
) : LastRouteIndexCache {
    override fun cache(
        start: Coordinate,
        end: Coordinate,
        routeIds: List<String>,
        ttl: Duration
    ) {
        try {
            val key = getKey(start, end)
            lastRouteIndexRedisTemplate.executePipelined { connection ->
                lastRouteIndexRedisTemplate.delete(key)
                lastRouteIndexRedisTemplate.opsForList().rightPushAll(key, routeIds)
                lastRouteIndexRedisTemplate.expire(key, ttl)
                null
            }
        } catch (_: Exception) {
            // Redis 캐시 실패 시 무시
        }
    }

    override fun get(
        start: Coordinate,
        end: Coordinate
    ): List<String> {
        return try {
            lastRouteIndexRedisTemplate.opsForList().range(getKey(start, end), 0, -1) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getKey(
        start: Coordinate,
        end: Coordinate
    ): String {
        return "routes:coordinate:${start.lon},${start.lat}:${end.lon},${end.lat}"
    }
}
