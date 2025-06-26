package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.BusTimeTableCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Component
class BusTimeTableRedisCache(
    private val busTimeTableRedisTemplate: RedisTemplate<String, BusTimeTable>
) : BusTimeTableCache {
    override fun get(
        routeName: String,
        busStation: BusStationMeta
    ): BusTimeTable? {
        val key = getKey(routeName, busStation)
        return busTimeTableRedisTemplate.opsForValue().get(key)
    }

    override fun cache(
        routeName: String,
        busStation: BusStationMeta,
        busTimeTable: BusTimeTable
    ) {
        val key = getKey(routeName, busStation)
        val ttlSeconds = calculateTtlUntilMidnight()
        busTimeTableRedisTemplate.opsForValue().set(key, busTimeTable, ttlSeconds, TimeUnit.SECONDS)
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
