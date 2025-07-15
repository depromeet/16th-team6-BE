package com.deepromeet.atcha.notification.domain

import com.deepromeet.atcha.userroute.domain.UserRoute
import org.springframework.stereotype.Component

private const val DEFAULT_TITLE = "앗차"

@Component
class NotificationContentManager {
    fun createPushNotification(
        userRoute: UserRoute,
        type: NotificationType
    ): NotificationContent {
        return createNotificationContent(userRoute, type)
    }

    private fun createNotificationContent(
        notification: UserRoute,
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
