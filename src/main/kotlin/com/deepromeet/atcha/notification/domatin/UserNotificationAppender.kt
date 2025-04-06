package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class UserNotificationAppender(
    private val userNotificationRepository: UserNotificationRepository
) {
    fun saveUserNotification(
        userNotification: UserNotification,
        userNotificationFrequency: UserNotificationFrequency
    ) = userNotificationRepository.save(userNotification, userNotificationFrequency)

    fun deleteUserNotification(
        userId: Long,
        routeId: String
    ) = userNotificationRepository.delete(userId, routeId)
}
