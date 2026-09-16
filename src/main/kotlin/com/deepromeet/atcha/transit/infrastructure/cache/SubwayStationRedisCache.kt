package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheStore
import com.deepromeet.atcha.transit.application.subway.SubwayStationCache
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class SubwayStationRedisCache(
    redisTemplate: RedisTemplate<String, SubwayStation>,
    cacheHitRecorder: RedisCacheHitRecorder
) : SubwayStationCache {
    private val store = RedisCacheStore(redisTemplate, cacheHitRecorder, metric = "subway-stations")

    override fun get(
        subwayLine: SubwayLine,
        stationName: String
    ): SubwayStation? = store.get(getKey(subwayLine, stationName))

    override fun cache(
        subwayLine: SubwayLine,
        stationName: String,
        subwayStation: SubwayStation
    ) = store.put(getKey(subwayLine, stationName), subwayStation)

    private fun getKey(
        subwayLine: SubwayLine,
        stationName: String
    ): String = "subway:station:${subwayLine.lnCd}:$stationName"
}
