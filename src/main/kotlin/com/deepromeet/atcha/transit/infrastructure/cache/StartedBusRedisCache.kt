package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheHitRecorder
import com.deepromeet.atcha.shared.infrastructure.cache.RedisCacheStore
import com.deepromeet.atcha.transit.application.bus.StartedBusCache
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class StartedBusRedisCache(
    redisTemplate: RedisTemplate<String, BusPosition>,
    cacheHitRecorder: RedisCacheHitRecorder
) : StartedBusCache {
    private val store = RedisCacheStore(redisTemplate, cacheHitRecorder)

    override fun get(id: String): BusPosition? = store.get(getKey(id))

    override fun cache(
        id: String,
        pos: BusPosition
    ) = store.put(getKey(id), pos, TTL)

    private fun getKey(id: String): String = "started-bus:$id"

    companion object {
        private val TTL = Duration.ofHours(3)
    }
}
