package com.deepromeet.atcha.notification.domatin

interface UserNotificationRepository {
    fun save(userNotification: UserNotification): UserNotification

    fun findById(
        userId: Long,
        routeId: String
    ): List<UserNotification>

    fun findAll(): List<UserNotification>

    fun updateDelayNotificationFlags(userNotification: UserNotification)

    fun update(userNotification: UserNotification)

    fun findByTime(time: String): List<UserNotification>

    fun hasNotification(userNotification: UserNotification): Boolean

    fun delete(
        userId: Long,
        routeId: String
    )
}
