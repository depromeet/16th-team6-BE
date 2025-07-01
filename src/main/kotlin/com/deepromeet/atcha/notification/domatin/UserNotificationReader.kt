package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class UserNotificationReader(
    private val userNotificationRepository: UserNotificationRepository
) {
    fun findById(
        userId: Long,
        routeId: String
    ): List<UserNotification> = userNotificationRepository.findById(userId, routeId)

    fun findByTime(currentTime: String): List<UserNotification> {
        return userNotificationRepository.findByTime(currentTime)
    }

    fun findAll(): List<UserNotification> = userNotificationRepository.findAll()

    fun hasNotification(userNotification: UserNotification): Boolean =
        userNotificationRepository.hasNotification(userNotification)
}
