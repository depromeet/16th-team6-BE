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
}
