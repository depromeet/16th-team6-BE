package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingProvider
import com.deepromeet.atcha.notification.domatin.NotificationContentManager
import com.deepromeet.atcha.notification.domatin.UserNotification
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.Duration

private const val MAX_RETRY_COUNT = 3
private const val PAYLOAD = "payload"
private const val RETRY_COUNT = "retryCount"

@Service
class RedisStreamConsumer(
    private val messagingProvider: MessagingProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val notificationContentManager: NotificationContentManager,
    private val redisStreamProducer: RedisStreamProducer,
    private val objectMapper: ObjectMapper,
    @Value("\${redis.stream.notification.key}")
    private val streamKey: String,
    @Value("\${redis.stream.notification.group}")
    private val groupName: String
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val streamOps = redisTemplate.opsForStream<String, String>()
    private val consumerId: String = InetAddress.getLocalHost().hostName

    @Scheduled(cron = "0 0-3,21-23 * * ?")
    fun startConsumer() {
        consumeStreamMessages()
    }

    private fun consumeStreamMessages() {
        val messages =
            streamOps.read(
                Consumer.from(groupName, consumerId),
                StreamReadOptions.empty()
                    .block(Duration.ofSeconds(1)),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            )
        messages?.forEach { message ->
            val json = message.value[PAYLOAD]
            val userNotification = objectMapper.readValue(json, UserNotification::class.java)
            val content = notificationContentManager.createPushNotification(userNotification)
            val messaging = Messaging(content, userNotification.token)
            if (messagingProvider.send(messaging)) {
                streamOps.acknowledge(streamKey, groupName, message.id)
                return@forEach
            }
            handleRetry(message, userNotification)
        }
    }

    private fun handleRetry(
        message: MapRecord<String, String, String>,
        userNotification: UserNotification
    ) {
        val retryCount = message.value[RETRY_COUNT]?.toInt() ?: 0
        if (retryCount < MAX_RETRY_COUNT) {
            redisStreamProducer.produce(userNotification, retryCount + 1)
            return
        }
        redisStreamProducer.produceToDeadLetter(userNotification, retryCount)
        log.error("💥FCM 재전송 횟수 초과: $userNotification")
    }
}
