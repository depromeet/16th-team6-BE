package com.deepromeet.atcha.notification.domatin

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserNotification(
    val userNotificationFrequency: UserNotificationFrequency,
    val initialDepartureTime: String,
    val updatedDepartureTime: String,
    val notificationTime: String,
    val token: String,
    val lastRouteId: String,
    val isDelayNotified: Boolean = false,
    val userId: Long
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    constructor(
        frequency: UserNotificationFrequency,
        token: String,
        notificationTime: LocalDateTime,
        departureTime: LocalDateTime,
        routeId: String,
        userId: Long
    ) : this(
        userNotificationFrequency = frequency,
        token = token,
        notificationTime = notificationTime.format(dateTimeFormatter),
        initialDepartureTime = departureTime.format(dateTimeFormatter),
        updatedDepartureTime = departureTime.format(dateTimeFormatter),
        lastRouteId = routeId,
        userId = userId
    )

    override fun toString(): String {
        return "UserNotification(userNotificationFrequency=$userNotificationFrequency, " +
            "initialDepartureTime='$initialDepartureTime', " +
            "updatedDepartureTime='$updatedDepartureTime', " +
            "notificationTime='$notificationTime', " +
            "token='$token', lastRouteId='$lastRouteId', " +
            "isDelayNotified=$isDelayNotified, userId=$userId)"
    }
}
