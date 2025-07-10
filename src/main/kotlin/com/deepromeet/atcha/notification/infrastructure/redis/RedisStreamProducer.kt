package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.domatin.UserLastRoute
import com.deepromeet.atcha.notification.domatin.UserLastRouteStreamProducer
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
) : UserLastRouteStreamProducer {
    private val streamOps = redisTemplate.opsForStream<String, String>()

    override fun produce(
        userLastRoute: UserLastRoute,
        retryCount: Int
    ) {
        val record =
            StreamRecords.newRecord()
                .`in`(group)
                .ofMap(
                    mapOf(
                        PAYLOAD to objectMapper.writeValueAsString(userLastRoute),
                        RETRY_COUNT to retryCount.toString()
                    )
                )
                .withStreamKey(key)
        streamOps.add(record)
    }

    override fun produceAll(userLastRoutes: List<UserLastRoute>) {
        userLastRoutes.forEach { userNotification -> produce(userNotification) }
    }

    override fun produceToDeadLetter(
        userLastRoute: UserLastRoute,
        retryCount: Int
    ) {
        val record =
            StreamRecords.newRecord()
                .ofMap(
                    mapOf(
                        PAYLOAD to objectMapper.writeValueAsString(userLastRoute),
                        RETRY_COUNT to retryCount.toString()
                    )
                )
                .withStreamKey(deadLetterKey)
        streamOps.add(record)
    }
}
