package com.deepromeet.atcha.notification.domatin

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

    fun createSuggestNotification(): NotificationContent {
        val body = "지금 밖이세요? 막차 알림 등록하고 편히 귀가하세요. \uD83C\uDFE0"
        return NotificationContent(
            title = DEFAULT_TITLE,
            body = body,
            dataMap =
                mutableMapOf(
                    "type" to NotificationType.SUGGESTION.toString(),
                    "title" to DEFAULT_TITLE,
                    "body" to body
                )
        )
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
