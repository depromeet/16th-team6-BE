package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationStreamProducer
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisStreamProducer(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) : UserNotificationStreamProducer {
    private val streamOps = redisTemplate.opsForStream<String, String>()

    override fun produce(userNotification: UserNotification) {
        val json = objectMapper.writeValueAsString(userNotification)
        StreamRecords.mapBacked<String, String, String>(mapOf("userNotification" to json))
//        streamOps.add()
    }

    override fun produceAll(userNotifications: List<UserNotification>) {
    }
}
