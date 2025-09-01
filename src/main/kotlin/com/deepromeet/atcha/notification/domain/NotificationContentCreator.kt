package com.deepromeet.atcha.notification.domain

import org.springframework.stereotype.Component

private const val DEFAULT_TITLE = "앗차"

@Component
class NotificationContentCreator {
    fun createPushContent(data: NotificationData): NotificationContent {
        return when (data) {
            is RouteRefreshNotificationData -> createRouteRefreshNotification(data)
        }
    }

    private fun createRouteRefreshNotification(data: RouteRefreshNotificationData): NotificationContent {
        return NotificationContent(
            title = DEFAULT_TITLE,
            body = data.departureTime.toString(),
            dataMap =
                mutableMapOf(
                    "title" to DEFAULT_TITLE,
                    "body" to (data.departureTime.toString()),
                    "type" to NotificationType.REFRESH.toString(),
                    "updatedAt" to (data.updatedAt)
                )
        )
    }
}
