package com.deepromeet.atcha.notification.infrastructure.scheduler

import com.deepromeet.atcha.common.redis.LockRedisManager
import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingManager
import com.deepromeet.atcha.notification.domatin.NotificationContentManager
import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationReader
import com.deepromeet.atcha.transit.domain.RouteDepartureTimeRefresher
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class NotificationScheduler(
    private val routeDepartureTimeRefresher: RouteDepartureTimeRefresher,
    private val userNotificationReader: UserNotificationReader,
    private val messagingManager: MessagingManager,
    private val notificationContentManager: NotificationContentManager,
    private val lockRedisManager: LockRedisManager
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

        val notifications = userNotificationReader.findByTime(currentMinute)
        logger.info("Found ${notifications.size} notifications to send")

        notifications.forEach { userNotification ->
            val sendSuccess =
                lockRedisManager.processWithLock(getSchedulerLockKey(userNotification)) {
                    try {
                        val pushNotification = notificationContentManager.createPushNotification(userNotification)
                        val messaging = Messaging(pushNotification, userNotification.token)
                        messagingManager.send(messaging)
                        true
                    } catch (e: Exception) {
                        logger.error("Notification Exception: ${e.message}", e)
                        false
                    }
                }
            if (sendSuccess) {
                logger.info("Successfully sent userNotification to token: ${userNotification.token}")
            } else {
                logger.error("Failed to send userNotification to token: ${userNotification.token}")
            }
        }
    }

    private fun getSchedulerLockKey(userNotification: UserNotification) =
        "lock:notification:${userNotification.userId}:${userNotification.lastRouteId}:" +
            "${userNotification.userNotificationFrequency.name}"
}
