package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingProvider
import com.deepromeet.atcha.notification.domatin.NotificationContentManager
import com.deepromeet.atcha.notification.domatin.UserNotification
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisStreamConsumer(
    private val messagingProvider: MessagingProvider,
    private val redisTemplate: RedisTemplate<String, String>,
    private val notificationContentManager: NotificationContentManager,
    private val objectMapper: ObjectMapper,
    @Value("\${redis.stream.notification.key}")
    private val streamKey: String,
    @Value("\${redis.stream.notification.group}")
    private val groupName: String
) {
    private val streamOps = redisTemplate.opsForStream<String, String>()
    private val log = LoggerFactory.getLogger(javaClass)
    private var consumerJob: Job? = null

    @Scheduled(cron = "0 0 13 * * *")
    fun startConsumer() {
        consumerJob =
            CoroutineScope(Dispatchers.IO).launch {
                log.info("🏈 Consumer 동작 시작")
                while (true) {
                    consumeStreamMessages()
                    Thread.sleep(1000 * 60)
                }
            }
    }

    @Scheduled(cron = "0 0 13 30 * *")
    fun stopConsumer() {
        consumerJob?.cancel()
        log.info("👻 Consumer 동작 종료")
    }

    private fun consumeStreamMessages() {
        try {
            val messages =
                streamOps.read(
                    Consumer.from(groupName, streamKey),
                    StreamReadOptions.empty()
                        .block(Duration.ofSeconds(1)),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                )
            messages?.forEach { message ->
                val json = message.value["payload"]
                val userNotification = objectMapper.readValue(json, UserNotification::class.java)
                val content = notificationContentManager.createPushNotification(userNotification)
                val messaging = Messaging(content, userNotification.token)
                messagingProvider.send(messaging)
            }
        } catch (e: Exception) {
            log.warn(e.message, e)
        }
    }
}
