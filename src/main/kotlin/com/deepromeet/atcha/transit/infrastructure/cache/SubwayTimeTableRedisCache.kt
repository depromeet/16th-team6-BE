package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.transit.application.subway.SubwayTimeTableCache
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class SubwayTimeTableRedisCache(
    private val subwayTimeTableRedisTemplate: RedisTemplate<String, SubwayTimeTable>,
    private val cacheHitRecorder: RedisCacheHitRecorder
) : SubwayTimeTableCache {
    override fun get(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable? {
        val key = getKey(startStation, dailyType, direction)
        return try {
            val timeTable = subwayTimeTableRedisTemplate.opsForValue().get(key)
            cacheHitRecorder.record("timetable:subway", timeTable != null)
            timeTable
        } catch (e: Exception) {
            logger.warn { "지하철 시간표 캐시 조회 중 오류 발생: ${e.message}" }
            cacheHitRecorder.record("timetable:subway", false)
            null
        }
    }

    override fun cache(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection,
        timeTable: SubwayTimeTable
    ) {
        val key = getKey(startStation, dailyType, direction)
        try {
            subwayTimeTableRedisTemplate.opsForValue().set(
                key,
                timeTable,
                Duration.ofDays(30)
            )
        } catch (e: Exception) {
            logger.warn { "지하철 시간표 캐시 저장 중 오류 발생: ${e.message}" }
        }
    }

    private fun getKey(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): String {
        return "routes:time:subway:${startStation.routeCode}-${startStation.name}:${dailyType.code}:${direction.name}"
    }
}
