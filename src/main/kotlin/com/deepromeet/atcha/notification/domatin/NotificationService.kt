package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.LastRouteReader
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationService(
    private val lastRouteReader: LastRouteReader,
    private val userReader: UserReader,
    private val userNotificationManager: UserNotificationManager,
    private val messagingManager: MessagingManager,
    private val notificationContentManager: NotificationContentManager
) {
    fun addRouteNotification(
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
                val frequency = UserNotificationFrequency.fromMinutes(minute)
                val userNotification =
                    UserNotification(
                        frequency = frequency,
                        token = notificationToken,
                        notificationTime = notificationTime,
                        departureTime = departureTime,
                        routeId = lastRouteId,
                        userId = user.id
                    )
                userNotificationManager.saveUserNotification(userNotification)
            }
        }
    }

    fun deleteRouteNotification(
        id: Long,
        lastRouteId: String
    ) {
        val user = userReader.read(id)
        userNotificationManager.deleteUserNotification(user.id, lastRouteId)
    }

    fun suggestRouteNotification(
        id: Long,
        coordinate: Coordinate
    ) {
        val user = userReader.read(id)
        val distance = coordinate.distanceTo(Coordinate(user.address.lat, user.address.lon))
        if (distance > 1.0) {
            val token = user.fcmToken
            val suggestNotification = notificationContentManager.createSuggestNotification()
            messagingManager.send(suggestNotification, token)
        }
    }
}
