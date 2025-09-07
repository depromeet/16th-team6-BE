package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.transit.application.subway.SubwayStationCache
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class SubwayStationRedisCache(
    private val redisTemplate: RedisTemplate<String, SubwayStation>,
    private val cacheHitRecorder: RedisCacheHitRecorder
) : SubwayStationCache {
    private val log = KotlinLogging.logger {}

    override fun get(
        subwayLine: SubwayLine,
        stationName: String
    ): SubwayStation? {
        val key = getKey(subwayLine, stationName)
        return try {
            val station = redisTemplate.opsForValue().get(key)
            cacheHitRecorder.record("subway-stations", station != null)
            station
        } catch (e: Exception) {
            log.warn { "지하철 역 캐시 조회 중 오류 발생: ${e.message}" }
            cacheHitRecorder.record("subway-stations", false)
            null
        }
    }

    override fun cache(
        subwayLine: SubwayLine,
        stationName: String,
        subwayStation: SubwayStation
    ) {
        val key = getKey(subwayLine, stationName)
        try {
            redisTemplate.opsForValue().set(key, subwayStation)
        } catch (e: Exception) {
            log.warn { "지하철 역 캐시 저장 중 오류 발생: ${e.message}" }
        }
    }

    private fun getKey(
        subwayLine: SubwayLine,
        stationName: String
    ): String {
        return "subway:station:${subwayLine.lnCd}:$stationName"
    }
}
