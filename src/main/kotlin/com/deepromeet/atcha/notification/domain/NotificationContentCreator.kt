package com.deepromeet.atcha.notification.domain

import org.springframework.stereotype.Component

private const val DEFAULT_TITLE = "앗차"

@Component
class NotificationContentCreator {
    fun createPushContent(
        data: NotificationData,
        isReal: Boolean
    ): NotificationContent {
        return when (data) {
            is RouteRefreshNotificationData -> createRouteRefreshNotification(data, isReal)
        }
    }

    private fun createRouteRefreshNotification(
        data: RouteRefreshNotificationData,
        isReal: Boolean
    ): NotificationContent {
        return NotificationContent(
            title = DEFAULT_TITLE,
            body = data.departureTime,
            dataMap =
                mutableMapOf(
                    "title" to DEFAULT_TITLE,
                    "body" to (data.departureTime),
                    "type" to NotificationType.REFRESH.toString(),
                    "updatedAt" to (data.updatedAt),
                    "isReal" to isReal.toString()
                )
        )
    }
}
