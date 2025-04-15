package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserNotificationAppender(
    private val userNotificationRepository: UserNotificationRepository
) {
    fun saveUserNotification(userNotification: UserNotification) = userNotificationRepository.save(userNotification)

    fun updateDelayNotificationFlags(userNotification: UserNotification) =
        userNotificationRepository.updateDelayNotificationFlags(
            userNotification
        )

    fun updateDepartureNotification(
        userNotification: UserNotification,
        newDepartureTime: LocalDateTime
    ) = userNotificationRepository.updateNotificationDepartureTime(userNotification, newDepartureTime)

    fun deleteUserNotification(
        userId: Long,
        routeId: String
    ) = userNotificationRepository.delete(userId, routeId)
}
