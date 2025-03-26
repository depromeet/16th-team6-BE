package com.deepromeet.atcha.notification.domatin

data class UserNotification(
    val notificationFrequency: NotificationFrequency,
    val initialDepartureTime: String,
    val updatedDepartureTime: String,
    val notificationTime: String,
    val notificationToken: String,
    val routeId: String,
    val isDelayNotified: Boolean = false,
    val userId: Long
)
