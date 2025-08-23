package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.transit.application.bus.BusTimeTableCache
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Component
class BusTimeTableRedisCache(
    private val busTimeTableRedisTemplate: RedisTemplate<String, BusSchedule>,
    private val cacheHitRecorder: RedisCacheHitRecorder
) : BusTimeTableCache {
    override fun get(
        routeName: String,
        busStation: BusStationMeta
    ): BusSchedule? {
        val key = getKey(routeName, busStation)
        val schedule = busTimeTableRedisTemplate.opsForValue().get(key)
        cacheHitRecorder.record("timetable:bus", schedule != null)
        return schedule
    }

    override fun cache(
        routeName: String,
        busStation: BusStationMeta,
        busSchedule: BusSchedule
    ) {
        val key = getKey(routeName, busStation)
        val ttlSeconds = calculateTtlUntilMidnight()
        busTimeTableRedisTemplate.opsForValue().set(key, busSchedule, ttlSeconds, TimeUnit.SECONDS)
    }

    private fun getKey(
        routeName: String,
        busStation: BusStationMeta
    ): String {
        return "routes:time:bus:$routeName:${busStation.coordinate.lat},${busStation.coordinate.lon}"
    }

    private fun calculateTtlUntilMidnight(): Long {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return ChronoUnit.SECONDS.between(now, midnight)
    }
}
