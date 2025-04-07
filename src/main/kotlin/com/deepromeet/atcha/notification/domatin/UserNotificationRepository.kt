package com.deepromeet.atcha.notification.domatin

import java.time.LocalDateTime

interface UserNotificationRepository {
    fun save(userNotification: UserNotification)

    fun findById(
        userId: Long,
        routeId: String
    ): List<UserNotification>

    fun findAll(): List<UserNotification>

    fun updateDelayNotificationFlags(userNotification: UserNotification)

    fun updateNotificationDepartureTime(
        userNotification: UserNotification,
        newDepartureTime: LocalDateTime
    )

    fun findByTime(time: String): List<UserNotification>

    fun hasNotification(userNotification: UserNotification): Boolean

    fun delete(
        userId: Long,
        routeId: String
    )
}
