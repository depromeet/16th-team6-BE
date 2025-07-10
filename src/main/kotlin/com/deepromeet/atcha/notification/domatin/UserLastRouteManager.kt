package com.deepromeet.atcha.notification.domatin

import org.springframework.stereotype.Component

@Component
class UserLastRouteManager(
    private val userLastRouteRepository: UserLastRouteRepository
) {
    fun saveUserNotification(userLastRoute: UserLastRoute) = userLastRouteRepository.save(userLastRoute)

    fun deleteUserNotification(
        userId: Long,
        routeId: String
    ) = userLastRouteRepository.delete(userId, routeId)
}
