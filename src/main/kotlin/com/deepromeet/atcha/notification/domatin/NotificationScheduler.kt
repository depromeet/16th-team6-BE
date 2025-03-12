package com.deepromeet.atcha.notification.domatin

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NotificationScheduler(
    private val redisOperations: RouteNotificationRedisOperations,
    private val firebaseMessaging: FirebaseMessaging
) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)

    @Scheduled(fixedRate = 60000) // 1분(60000ms) 간격으로 실행
    fun checkAndSendNotifications() {
        val now = LocalDateTime.now()
        val currentMinute = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        logger.info("Checking notifications for time: $currentMinute")

        val notifications = redisOperations.findNotificationsByMinute(currentMinute)
        logger.info("Found ${notifications.size} notifications to send")

        notifications.forEach { notification ->
            try {
                sendPushNotification(notification)
                redisOperations.deleteNotification(notification)
                logger.info("Successfully sent notification to token: ${notification.notificationToken}")
            } catch (e: Exception) {
                logger.error("Failed to send notification: ${e.message}", e)
            }
        }
    }

    private fun sendPushNotification(notification: UserNotification) {
        val message =
            Message.builder()
                .setToken(notification.notificationToken)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle("앗차")
                        .setBody("출발하기 ${notification.notificationFrequency}분 전입니다.")
                        .build()
                )
                .build()

        firebaseMessaging.send(message)
    }
}
