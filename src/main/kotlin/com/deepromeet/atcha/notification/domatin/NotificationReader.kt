package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class NotificationReader(
    private val notificationRepository: NotificationRepository
) {
    fun findById(
        userId: Long,
        routeId: String
    ): List<UserNotification> = notificationRepository.findById(userId, routeId)
}
