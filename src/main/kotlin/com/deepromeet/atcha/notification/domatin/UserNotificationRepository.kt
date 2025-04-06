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

    fun findByTime(time: String): List<UserNotification>

    fun hasNotification(userNotification: UserNotification): Boolean

    fun delete(
        userId: Long,
        routeId: String
    )
}
