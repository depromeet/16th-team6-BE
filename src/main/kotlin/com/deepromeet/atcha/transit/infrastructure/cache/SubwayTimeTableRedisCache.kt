package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheStore
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
    subwayTimeTableRedisTemplate: RedisTemplate<String, SubwayTimeTable>,
    cacheHitRecorder: RedisCacheHitRecorder
) : SubwayTimeTableCache {
    private val store = RedisCacheStore(subwayTimeTableRedisTemplate, cacheHitRecorder, metric = "timetable:subway")

    override fun get(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable? = store.get(getKey(startStation, dailyType, direction))

    override fun cache(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection,
        timeTable: SubwayTimeTable
    ) = store.put(getKey(startStation, dailyType, direction), timeTable, TTL)

    private fun getKey(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): String = "routes:time:subway:${startStation.routeCode}-${startStation.name}:${dailyType.code}:${direction.name}"

    companion object {
        private val TTL = Duration.ofDays(30)
    }
}
