package com.deepromeet.atcha.notification.domatin

interface UserNotificationRepository {
    fun save(
        userNotification: UserNotification,
        userNotificationFrequency: UserNotificationFrequency
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
