package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.notification.domatin.MessagingManager
import com.deepromeet.atcha.notification.domatin.NotificationContentManager
import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationManager
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class DepartureDelayNotifier(
    private val notificationContentManager: NotificationContentManager,
    private val messagingManager: MessagingManager,
    private val userNotificationManager: UserNotificationManager
) {
    fun notifyIfDelayed(notification: UserNotification) {
        val diffMinutes = calculateDiffMinutes(notification.initialDepartureTime, notification.updatedDepartureTime)

        if (diffMinutes >= 10 && !notification.isDelayNotified) {
            val notificationContent = notificationContentManager.createDelayPushNotification(notification)
            messagingManager.send(notificationContent, notification.token)
            userNotificationManager.updateDelayNotificationFlags(notification)
        }
    }

    private fun calculateDiffMinutes(
        controlTime: String,
        treatmentTime: String
    ): Long {
        val control = LocalDateTime.parse(controlTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val treatment = LocalDateTime.parse(treatmentTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return Duration.between(control, treatment).toMinutes()
    }
}
