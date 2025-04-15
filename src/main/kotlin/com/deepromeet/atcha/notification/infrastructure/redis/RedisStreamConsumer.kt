package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingProvider
import com.deepromeet.atcha.notification.domatin.NotificationContentManager
import com.deepromeet.atcha.notification.domatin.UserNotification
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.time.Duration

private const val PAYLOAD = "payload"

@Service
class RedisStreamConsumer(
    private val messagingProvider: MessagingProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val notificationContentManager: NotificationContentManager,
    private val objectMapper: ObjectMapper,
    @Value("\${redis.stream.notification.key}")
    private val streamKey: String,
    @Value("\${redis.stream.notification.group}")
    private val groupName: String,
    @Value("\${redis.stream.notification.dead-letter.key}")
    private val deadLetterKey: String
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val streamOps = redisTemplate.opsForStream<String, String>()
    private val consumerId: String = InetAddress.getLocalHost().hostName

    @Scheduled(cron = "*/10 0 0-3,21-23 * * ?")
    fun consumeStreamMessages() {
        val messages =
            streamOps.read(
                Consumer.from(groupName, consumerId),
                StreamReadOptions.empty()
                    .block(Duration.ofSeconds(1)),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            )
        messages?.forEach { message ->
            val messaging = createMessaging(message)
            if (messagingProvider.send(messaging)) {
                log.info("⭐️알림 전송 성공")
                streamOps.acknowledge(streamKey, groupName, message.id)
                return@forEach
            }
            log.info("⚠️️알림 전송 실패")
        }
    }

    @Scheduled(cron = "*/10 0 0-3,21-23 * * ?")
    fun reclaimPendingMessages() {
        val pendingMessages =
            streamOps.pending(
                streamKey,
                groupName,
                Range.unbounded<RecordId>(),
                100
            )

        val reclaimRecordIds: List<RecordId> =
            pendingMessages
                .filter { it.totalDeliveryCount > 1 }
                .map { it.id }
                .toList()

        if (reclaimRecordIds.isEmpty()) return

        val claimedMessages: List<MapRecord<String, String, String>> =
            reclaimRecordIds.flatMap { recordId ->
                streamOps.claim(
                    streamKey,
                    groupName,
                    consumerId,
                    Duration.ofMinutes(3),
                    recordId
                )
            }

        claimedMessages.forEach { message ->
            val json = message.value[PAYLOAD]
            runCatching {
                objectMapper.readValue(json, UserNotification::class.java)
            }.getOrElse {
                streamOps.add(deadLetterKey, mapOf(PAYLOAD to json))
                streamOps.acknowledge(streamKey, groupName, message.id)
                return@forEach
            }

            val messaging = createMessaging(message)
            val success = runCatching { messagingProvider.send(messaging) }.getOrElse { false }

            handleDeadLetter(success, message, json)
        }
    }

    private fun createMessaging(message: MapRecord<String, String, String>): Messaging {
        val json = message.value[PAYLOAD]
        val userNotification = objectMapper.readValue(json, UserNotification::class.java)
        val content = notificationContentManager.createPushNotification(userNotification)
        val messaging = Messaging(content, userNotification.token)
        return messaging
    }

    private fun handleDeadLetter(
        success: Boolean,
        message: MapRecord<String, String, String>,
        json: String?
    ) {
        if (success) {
            streamOps.acknowledge(streamKey, groupName, message.id)
        } else {
            streamOps.add(deadLetterKey, mapOf(PAYLOAD to json))
            streamOps.acknowledge(streamKey, groupName, message.id)
        }
    }
}
