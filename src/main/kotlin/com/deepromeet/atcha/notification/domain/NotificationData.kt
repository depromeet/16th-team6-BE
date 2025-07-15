package com.deepromeet.atcha.notification.domain

sealed class NotificationData {
    abstract val userId: String
    abstract val token: String
    abstract val idempotencyKey: String
}

data class RouteRefreshNotificationData(
    override val userId: String,
    override val token: String,
    override val idempotencyKey: String,
    val departureTime: String,
    val updatedAt: String
) : NotificationData()
