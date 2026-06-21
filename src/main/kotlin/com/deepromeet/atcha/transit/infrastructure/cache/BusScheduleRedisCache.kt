package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheStore
import com.deepromeet.atcha.transit.application.bus.BusScheduleCache
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class BusScheduleRedisCache(
    busScheduleRedisTemplate: RedisTemplate<String, BusSchedule>,
    cacheHitRecorder: RedisCacheHitRecorder
) : BusScheduleCache {
    private val store = RedisCacheStore(busScheduleRedisTemplate, cacheHitRecorder, metric = "timetable:bus")

    override fun get(
        routeName: String,
        busStation: BusStationMeta
    ): BusSchedule? = store.get(getKey(routeName, busStation))

    override fun cache(
        routeName: String,
        busStation: BusStationMeta,
        busSchedule: BusSchedule
    ) = store.put(getKey(routeName, busStation), busSchedule, ttlUntilMidnight())

    private fun getKey(
        routeName: String,
        busStation: BusStationMeta
    ): String = "routes:time:bus:$routeName:${busStation.coordinate.lat},${busStation.coordinate.lon}"

    private fun ttlUntilMidnight(): Duration {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return Duration.between(now, midnight)
    }
}
