package com.deepromeet.atcha.notification.domatin

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserNotification(
    val notificationFrequency: NotificationFrequency,
    val initialDepartureTime: String,
    val updatedDepartureTime: String,
    val notificationTime: String,
    val notificationToken: String,
    val routeId: String,
    val isDelayNotified: Boolean = false,
    val userId: Long
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    constructor(
        frequency: NotificationFrequency,
        notificationToken: String,
        notificationTime: LocalDateTime,
        departureTime: LocalDateTime,
        routeId: String,
        userId: Long
    ) : this(
        notificationFrequency = frequency,
        notificationToken = notificationToken,
        notificationTime = notificationTime.format(dateTimeFormatter),
        initialDepartureTime = departureTime.format(dateTimeFormatter),
        updatedDepartureTime = departureTime.format(dateTimeFormatter),
        routeId = routeId,
        userId = userId
    )
}
