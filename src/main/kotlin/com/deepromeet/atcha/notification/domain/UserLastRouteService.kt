package com.deepromeet.atcha.notification.domain

import com.deepromeet.atcha.transit.domain.LastRouteReader
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserLastRouteService(
    private val lastRouteReader: LastRouteReader,
    private val userReader: UserReader,
    private val userLastRouteManager: UserLastRouteManager,
    private val messagingManager: MessagingManager,
    private val notificationContentManager: NotificationContentManager
) {
    fun addUserLastRoute(
        id: Long,
        lastRouteId: String
    ) {
        val route = lastRouteReader.read(lastRouteId)
        val departureTime = LocalDateTime.parse(route.departureDateTime)

        val user = userReader.read(id)
        val notificationToken = user.fcmToken

        user.alertFrequencies.forEach { minute ->
            val notificationTime = departureTime.minusMinutes(minute.toLong())
            if (notificationTime.isAfter(LocalDateTime.now())) {
                val userLastRoute =
                    UserLastRoute(
                        token = notificationToken,
                        departureTime = departureTime,
                        routeId = lastRouteId,
                        userId = user.id
                    )
                userLastRouteManager.saveUserNotification(userLastRoute)
            }
        }
    }

    fun deleteUserLastRoute(
        id: Long,
        lastRouteId: String
    ) {
        val user = userReader.read(id)
        userLastRouteManager.deleteUserNotification(user.id, lastRouteId)
    }
}
