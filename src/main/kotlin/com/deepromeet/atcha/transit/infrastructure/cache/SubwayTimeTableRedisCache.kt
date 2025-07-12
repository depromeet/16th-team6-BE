package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayStation
import com.deepromeet.atcha.transit.domain.SubwayTimeTable
import com.deepromeet.atcha.transit.domain.SubwayTimeTableCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Component
class SubwayTimeTableRedisCache(
    private val subwayTimeTableRedisTemplate: RedisTemplate<String, SubwayTimeTable>
) : SubwayTimeTableCache {
    override fun get(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable? {
        val key = getKey(startStation, dailyType, direction)
        return subwayTimeTableRedisTemplate.opsForValue().get(key)
    }

    override fun cache(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection,
        timeTable: SubwayTimeTable
    ) {
        val key = getKey(startStation, dailyType, direction)
        val ttlSeconds = calculateTtlUntilMidnight()
        subwayTimeTableRedisTemplate.opsForValue().set(key, timeTable, ttlSeconds, TimeUnit.SECONDS)
    }

    private fun getKey(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): String {
        return "time:subway:${startStation.name}:${dailyType.code}:${direction.name}"
    }

    private fun calculateTtlUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return ChronoUnit.SECONDS.between(now, midnight)
    }
}
