package com.deepromeet.atcha.support.fixture

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationFrequency
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

object UserNotificationFixture {
    fun create(
        userNotificationFrequency: UserNotificationFrequency = UserNotificationFrequency.ONE,
        initialDepartureTime: String = LocalDateTime.now().plus(Duration.ofMinutes(10)).format(dateTimeFormatter),
        updatedDepartureTime: String = LocalDateTime.now().format(dateTimeFormatter),
        notificationTime: String = LocalDateTime.now().format(dateTimeFormatter),
        notificationToken: String = "test-token",
        routeId: String = "route-123",
        isDelayNotified: Boolean = false,
        userId: Long = 1L
    ) = UserNotification(
        userNotificationFrequency = userNotificationFrequency,
        initialDepartureTime = initialDepartureTime,
        updatedDepartureTime = updatedDepartureTime,
        notificationTime = notificationTime,
        token = notificationToken,
        lastRouteId = routeId,
        isDelayNotified = isDelayNotified,
        userId = userId
    )
}
