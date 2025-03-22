package com.deepromeet.atcha.location.infrastructure.cache

import com.deepromeet.atcha.location.domain.POI
import com.deepromeet.atcha.location.domain.POIHistoryQueue
import com.deepromeet.atcha.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

const val LIST_FULL_SIZE: Long = 10L

private val logger = KotlinLogging.logger {}

@Component
class RedisPOIHistoryQueue(
    private val redisTemplate: RedisTemplate<String, POI>
) : POIHistoryQueue {
    override fun push(
        user: User,
        poi: POI
    ) {
        val key: String = getKey(user)
        logger.info { "Pushing POI to Redis: $poi" }
        redisTemplate.opsForList().leftPush(key, poi)
        redisTemplate.opsForList().trim(key, 0, LIST_FULL_SIZE - 1)
    }

    override fun pop(
        user: User,
        poi: POI
    ) {
        val key: String = getKey(user)
        logger.info { "Popping POI from Redis: $poi" }
        redisTemplate.opsForList().remove(key, 1, poi)
    }

    override fun popAll(user: User) {
        val key: String = getKey(user)
        redisTemplate.delete(key)
    }

    override fun getAll(user: User): List<POI> {
        val key: String = getKey(user)
        return redisTemplate.opsForList().range(key, 0, LIST_FULL_SIZE) ?: emptyList()
    }

    override fun exists(
        user: User,
        poi: POI
    ): Boolean {
        val key: String = getKey(user)
        return redisTemplate.opsForList().range(key, 0, LIST_FULL_SIZE)?.contains(poi) ?: false
    }

    private fun getKey(user: User): String {
        return "POI_HISTORY:${user.id}"
    }
}
