package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationStreamProducer
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

private const val PAYLOAD = "payload"
private const val RETRY_COUNT = "retryCount"

@Component
class RedisStreamProducer(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${redis.stream.notification.key}")
    private val key: String,
    @Value("\${redis.stream.notification.group}")
    private val group: String,
    @Value("\${redis.stream.notification.dead-letter.key}")
    private val deadLetterKey: String
) : UserNotificationStreamProducer {
    private val streamOps = redisTemplate.opsForStream<String, String>()

    override fun produce(
        userNotification: UserNotification,
        retryCount: Int
    ) {
        val record =
            StreamRecords.newRecord()
                .`in`(group)
                .ofMap(
                    mapOf(
                        PAYLOAD to objectMapper.writeValueAsString(userNotification),
                        RETRY_COUNT to retryCount.toString()
                    )
                )
                .withStreamKey(key)
        streamOps.add(record)
    }

    override fun produceAll(userNotifications: List<UserNotification>) {
        userNotifications.forEach { userNotification -> produce(userNotification) }
    }

    override fun produceToDeadLetter(
        userNotification: UserNotification,
        retryCount: Int
    ) {
        val record =
            StreamRecords.newRecord()
                .ofMap(
                    mapOf(
                        PAYLOAD to objectMapper.writeValueAsString(userNotification),
                        RETRY_COUNT to retryCount.toString()
                    )
                )
                .withStreamKey(deadLetterKey)
        streamOps.add(record)
    }
}
