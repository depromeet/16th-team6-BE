package com.deepromeet.atcha.route.infrastructure.cache

import com.deepromeet.atcha.route.application.LastRouteCache
import com.deepromeet.atcha.route.domain.LastRoute
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class LastRouteRedisCache(
    private val lastRouteRedisTemplate: RedisTemplate<String, LastRoute>
) : LastRouteCache {
    override fun get(routeId: String): LastRoute? {
        return try {
            lastRouteRedisTemplate.opsForValue().get(getKey(routeId))
        } catch (e: Exception) {
            logger.warn { "마지막 경로 정보 조회 중 오류 발생: ${e.message}" }
            null
        }
    }

    override fun cache(route: LastRoute) {
        try {
            lastRouteRedisTemplate.opsForValue().set(getKey(route.id), route, Duration.ofDays(1))
        } catch (e: Exception) {
            logger.warn { "마지막 경로 정보 저장 중 오류 발생: ${e.message}" }
        }
    }

    fun getKey(routeId: String): String {
        return "routes:last:$routeId"
    }
}
