package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.transit.application.bus.BusTimeTableCache
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

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
        return try {
            val schedule = busTimeTableRedisTemplate.opsForValue().get(key)
            cacheHitRecorder.record("timetable:bus", schedule != null)
            schedule
        } catch (e: Exception) {
            logger.warn { "버스 시간표 캐시 조회 중 오류 발생: ${e.message}" }
            cacheHitRecorder.record("timetable:bus", false)
            null
        }
    }

    override fun cache(
        routeName: String,
        busStation: BusStationMeta,
        busSchedule: BusSchedule
    ) {
        val key = getKey(routeName, busStation)
        val ttlSeconds = calculateTtlUntilMidnight()
        try {
            busTimeTableRedisTemplate.opsForValue().set(key, busSchedule, ttlSeconds, TimeUnit.SECONDS)
        } catch (e: Exception) {
            logger.warn { "버스 시간표 캐시 저장 중 오류 발생: ${e.message}" }
        }
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
