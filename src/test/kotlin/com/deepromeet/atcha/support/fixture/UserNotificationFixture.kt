package com.deepromeet.atcha.support.fixture

import com.deepromeet.atcha.notification.domatin.NotificationFrequency
import com.deepromeet.atcha.notification.domatin.UserNotification
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

object UserNotificationFixture {
    fun create(
        notificationFrequency: NotificationFrequency = NotificationFrequency.ONE,
        initialDepartureTime: String = LocalDateTime.now().format(dateTimeFormatter),
        updatedDepartureTime: String = LocalDateTime.now().format(dateTimeFormatter),
        notificationTime: String = LocalDateTime.now().format(dateTimeFormatter),
        notificationToken: String = "test-token",
        routeId: String = "route-123",
        isDelayNotified: Boolean = false,
        userId: Long = 1L
    ) = UserNotification(
        notificationFrequency = notificationFrequency,
        initialDepartureTime = initialDepartureTime,
        updatedDepartureTime = updatedDepartureTime,
        notificationTime = notificationTime,
        notificationToken = notificationToken,
        lastRouteId = routeId,
        isDelayNotified = isDelayNotified,
        userId = userId
    )
}
