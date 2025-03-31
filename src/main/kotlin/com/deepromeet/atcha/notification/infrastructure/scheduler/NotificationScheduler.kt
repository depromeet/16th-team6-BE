package com.deepromeet.atcha.notification.infrastructure.scheduler

import com.deepromeet.atcha.notification.domatin.NotificationManager
import com.deepromeet.atcha.transit.domain.RouteDepartureTimeRefresher
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class NotificationScheduler(
    private val routeDepartureTimeRefresher: RouteDepartureTimeRefresher,
    private val notificationManager: NotificationManager
) {
    private val logger = LoggerFactory.getLogger(NotificationScheduler::class.java)

    @Scheduled(cron = "0 * 0-3,21-23 * * ?")
    fun checkAndSendNotifications() {
        // 알림 업데이트
        routeDepartureTimeRefresher.refresh()

        // 현재 시간 기준으로 분 단위 알림 확인 -> 전송 -> 삭제
        val now = LocalDateTime.now()
        val currentMinute = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        logger.info("Checking notifications for time: $currentMinute")

        val notifications = notificationManager.findNotificationsByMinutes(currentMinute)
        logger.info("Found ${notifications.size} notifications to send")

        notifications.forEach { notification ->
            try {
                notificationManager.sendAndDeleteNotification(notification)
                logger.info("Successfully sent notification to token: ${notification.notificationToken}")
            } catch (e: Exception) {
                logger.error("Failed to send notification: ${e.message}", e)
            }
        }
    }
}
