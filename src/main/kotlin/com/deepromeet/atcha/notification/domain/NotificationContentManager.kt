package com.deepromeet.atcha.notification.domain

import org.springframework.stereotype.Component

private const val DEFAULT_TITLE = "앗차"

@Component
class NotificationContentManager {
    fun createPushNotification(
        notification: UserLastRoute,
        type: NotificationType
    ): NotificationContent {
        return createNotificationContent(notification, type)
    }

    private fun createNotificationContent(
        notification: UserLastRoute,
        type: NotificationType
    ): NotificationContent {
        return NotificationContent(
            title = DEFAULT_TITLE,
            body = notification.departureTime,
            dataMap =
                mutableMapOf(
                    "title" to DEFAULT_TITLE,
                    "body" to notification.departureTime,
                    "type" to type.toString(),
                    "updatedAt" to notification.updatedAt
                )
        )
    }
}
