package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.application.bus.StartedBusCache
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class StartedBusRedisCache(
    private val redisTemplate: RedisTemplate<String, BusPosition>
) : StartedBusCache {
    private val keyPrefix = "started-bus:"

    companion object {
        private val TTL = Duration.ofHours(3)
    }

    override fun get(id: String): BusPosition? {
        return try {
            redisTemplate.opsForValue().get(getKey(id))
        } catch (e: Exception) {
            logger.warn { "버스 위치 정보 캐시 조회 중 오류 발생: ${e.message}" }
            null
        }
    }

    override fun cache(
        id: String,
        pos: BusPosition
    ) {
        try {
            redisTemplate.opsForValue().set(keyPrefix + id, pos, TTL)
        } catch (e: Exception) {
            logger.warn { "버스 위치 정보 캐시 저장 중 오류 발생: ${e.message}" }
        }
    }

    private fun getKey(id: String): String = keyPrefix + id
}
