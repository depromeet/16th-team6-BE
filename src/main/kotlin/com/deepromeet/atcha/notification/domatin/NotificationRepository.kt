package com.deepromeet.atcha.notification.domatin

interface NotificationRepository {
    fun save(
        userNotification: UserNotification,
        notificationFrequency: NotificationFrequency
    )

    fun findById(
        userId: Long,
        routeId: String
    ): List<UserNotification>

    fun delete(
        userId: Long,
        routeId: String
    )
}
