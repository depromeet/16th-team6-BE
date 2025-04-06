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
    private val notificationAppender: NotificationAppender,
    private val notificationManager: NotificationManager
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
                val frequency = NotificationFrequency.fromMinutes(minute)
                val userNotification =
                    UserNotification(
                        frequency = frequency,
                        notificationToken = notificationToken,
                        notificationTime = notificationTime,
                        departureTime = departureTime,
                        routeId = lastRouteId,
                        userId = user.id
                    )
                notificationAppender.saveUserNotification(userNotification, frequency)
            }
        }
    }

    fun deleteRouteNotification(
        id: Long,
        lastRouteId: String
    ) {
        val user = userReader.read(id)
        notificationAppender.deleteUserNotification(user.id, lastRouteId)
    }

    fun test(id: Long) {
        val user = userReader.read(id)
        val notificationToken = user.fcmToken
        notificationManager.sendPushNotificationForTest(notificationToken)
    }

    fun suggestRouteNotification(
        id: Long,
        coordinate: Coordinate
    ) {
        val user = userReader.read(id)
        val distance = coordinate.distanceTo(Coordinate(user.address.lat, user.address.lon))
        if (distance > 1.0) {
            val notificationToken = user.fcmToken
            notificationManager.sendSuggestPushNotification(notificationToken)
        }
    }
}
