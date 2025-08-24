package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.transit.application.subway.SubwayTimeTableCache
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.subway.SubwayDirection
import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

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
        val timeTable = subwayTimeTableRedisTemplate.opsForValue().get(key)
        cacheHitRecorder.record("timetable:subway", timeTable != null)
        return timeTable
    }

    override fun cache(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection,
        timeTable: SubwayTimeTable
    ) {
        val key = getKey(startStation, dailyType, direction)
        subwayTimeTableRedisTemplate.opsForValue().set(
            key,
            timeTable,
            Duration.ofDays(30)
        )
    }

    private fun getKey(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): String {
        return "routes:time:subway:${startStation.routeCode}-${startStation.name}:${dailyType.code}:${direction.name}"
    }
}
