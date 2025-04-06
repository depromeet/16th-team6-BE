package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class NotificationAppender(
    private val notificationRepository: NotificationRepository
) {
    fun saveUserNotification(
        userNotification: UserNotification,
        notificationFrequency: NotificationFrequency
    ) = notificationRepository.save(userNotification, notificationFrequency)

    fun deleteUserNotification(
        userId: Long,
        routeId: String
    ) = notificationRepository.delete(userId, routeId)
}
