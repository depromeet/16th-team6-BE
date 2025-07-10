package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.notification.exception.NotificationError
import com.deepromeet.atcha.notification.exception.NotificationException
import org.springframework.stereotype.Component

@Component
class UserLastRouteReader(
    private val userLastRouteRepository: UserLastRouteRepository
) {
    fun findById(
        userId: Long,
        routeId: String
    ): UserLastRoute {
        return userLastRouteRepository.findById(userId, routeId)
            ?: throw NotificationException.of(
                NotificationError.NOTIFICATION_NOT_FOUND
            )
    }

    fun findAll(): List<UserLastRoute> = userLastRouteRepository.findAll()
}
