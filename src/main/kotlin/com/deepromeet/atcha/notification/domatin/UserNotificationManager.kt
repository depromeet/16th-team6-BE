package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class UserNotificationManager(
    private val userNotificationRepository: UserNotificationRepository
) {
    fun saveUserNotification(userNotification: UserNotification) = userNotificationRepository.save(userNotification)

    fun updateDelayNotificationFlags(userNotification: UserNotification) =
        userNotificationRepository.updateDelayNotificationFlags(
            userNotification
        )

    fun deleteUserNotification(
        userId: Long,
        routeId: String
    ) = userNotificationRepository.delete(userId, routeId)
}
