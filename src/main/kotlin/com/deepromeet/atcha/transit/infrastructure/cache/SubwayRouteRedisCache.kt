package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.transit.application.subway.SubwayRouteCache
import com.deepromeet.atcha.transit.domain.subway.Route
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class SubwayRouteRedisCache(
    private val subwayRouteRedisTemplate: RedisTemplate<String, List<Route>>,
    private val cacheHitRecorder: RedisCacheHitRecorder
) : SubwayRouteCache {
    private val log = KotlinLogging.logger {}

    override fun get(subwayLine: SubwayLine): List<Route>? {
        val key = getKey(subwayLine)
        return try {
            val routes = subwayRouteRedisTemplate.opsForValue().get(key)
            cacheHitRecorder.record("subway-routes", routes != null)
            routes
        } catch (e: Exception) {
            log.warn { "지하철 경로 캐시 조회 중 오류 발생: ${e.message}" }
            cacheHitRecorder.record("subway-routes", false)
            null
        }
    }

    override fun cache(
        subwayLine: SubwayLine,
        routes: List<Route>
    ) {
        val key = getKey(subwayLine)
        try {
            subwayRouteRedisTemplate.opsForValue().set(key, routes)
        } catch (e: Exception) {
            log.warn { "지하철 경로 캐시 저장 중 오류 발생: ${e.message}" }
        }
    }

    private fun getKey(subwayLine: SubwayLine): String {
        return "subway:routes:${subwayLine.lnCd}"
    }
}
