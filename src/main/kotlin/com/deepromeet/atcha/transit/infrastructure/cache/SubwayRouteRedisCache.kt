package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheStore
import com.deepromeet.atcha.transit.application.subway.SubwayRouteCache
import com.deepromeet.atcha.transit.domain.subway.Route
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class SubwayRouteRedisCache(
    subwayRouteRedisTemplate: RedisTemplate<String, List<Route>>,
    cacheHitRecorder: RedisCacheHitRecorder
) : SubwayRouteCache {
    private val store = RedisCacheStore(subwayRouteRedisTemplate, cacheHitRecorder, metric = "subway-routes")

    override fun get(subwayLine: SubwayLine): List<Route>? = store.get(getKey(subwayLine))

    override fun cache(
        subwayLine: SubwayLine,
        routes: List<Route>
    ) = store.put(getKey(subwayLine), routes)

    private fun getKey(subwayLine: SubwayLine): String = "subway:routes:${subwayLine.lnCd}"
}
