package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.StartedBusCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class StartedBusRedisCache(
    private val redisTemplate: RedisTemplate<String, BusPosition>
) : StartedBusCache {
    private val keyPrefix = "started-bus:"

    companion object {
        private val TTL = Duration.ofHours(3)
    }

    override fun get(id: String): BusPosition? = redisTemplate.opsForValue().get(getKey(id))

    override fun cache(
        id: String,
        pos: BusPosition
    ) {
        redisTemplate.opsForValue().set(keyPrefix + id, pos, TTL)
    }

    private fun getKey(id: String): String = keyPrefix + id
}
