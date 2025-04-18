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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
            val json = message.value[PAYLOAD]
            val userNotification = objectMapper.readValue(json, UserNotification::class.java)

            // ① 전송 시각이 아니면 패스 (ACK 하지 않아 다시 대기)
            if (!shouldSend(userNotification)) return@forEach

            // ② 전송 시각이면 메시지 생성 후 전송
            val messaging = createMessaging(userNotification)
            if (messagingProvider.send(messaging)) {
                log.info("⭐️알림 전송 성공")
                streamOps.acknowledge(streamKey, groupName, message.id) // ACK → 스트림에서 삭제
            } else {
                log.info("⚠️️알림 전송 실패")
            }
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
            val userNotification =
                runCatching {
                    objectMapper.readValue(json, UserNotification::class.java)
                }.getOrElse {
                    streamOps.add(deadLetterKey, mapOf(PAYLOAD to json))
                    streamOps.acknowledge(streamKey, groupName, message.id)
                    return@forEach
                }

            if (!shouldSend(userNotification)) return@forEach

            val messaging = createMessaging(userNotification)
            val success = runCatching { messagingProvider.send(messaging) }.getOrElse { false }
            handleDeadLetter(success, message, json)
        }
    }

    private fun createMessaging(userNotification: UserNotification): Messaging {
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

    private fun shouldSend(notification: UserNotification): Boolean {
        val target =
            LocalDateTime.parse(
                notification.notificationTime,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )

        val diffMinutes = Duration.between(LocalDateTime.now(), target).toMinutes()
        return diffMinutes in 0..1 && !notification.isDelayNotified
    }
}
