package com.deepromeet.atcha.userroute.infrastructure.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import io.lettuce.core.RedisBusyException
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.RedisSystemException
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisStreamInitializer(
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${redis.stream.notification.key}")
    private val streamKey: String,
    @Value("\${redis.stream.notification.group}")
    private val groupName: String
) : InitializingBean {
    private val streamOps = redisTemplate.opsForStream<String, String>()
    private val log = KotlinLogging.logger {}

    override fun afterPropertiesSet() {
        initIfAbsent()
        createGroup()
    }

    private fun initIfAbsent() {
        if (!redisTemplate.hasKey(streamKey)) {
            streamOps.add(
                StreamRecords.mapBacked<String?, String?, String?>(mapOf("init" to "init"))
                    .withStreamKey(streamKey)
            )
        }
    }

    private fun createGroup() {
        try {
            streamOps.createGroup(streamKey, groupName)
        } catch (e: RedisSystemException) {
            if (e.rootCause is RedisBusyException &&
                (e.rootCause as RedisBusyException).message?.contains("BUSYGROUP") == true
            ) {
                log.info { "Consumer Group '$groupName' already exists in stream '$streamKey'" }
            } else {
                throw e
            }
        }
    }
}
