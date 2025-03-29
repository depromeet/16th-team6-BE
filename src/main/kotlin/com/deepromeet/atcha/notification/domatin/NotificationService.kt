package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.notification.api.NotificationRequest
import com.deepromeet.atcha.transit.domain.LastRouteReader
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class NotificationService(
    private val redisOperations: RouteNotificationRedisOperations,
    private val lastRouteReader: LastRouteReader,
    private val userReader: UserReader,
    private val notificationManager: NotificationManager
) {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun addRouteNotification(
        id: Long,
        request: NotificationRequest
    ) {
        val route = lastRouteReader.read(request.lastRouteId)
        val departureTime = LocalDateTime.parse(route.departureDateTime)

        val user = userReader.read(id)
        val notificationToken = user.fcmToken

        user.alertFrequencies.forEach { minute ->
            val notificationDateTime = departureTime.minusMinutes(minute.toLong())
            if (notificationDateTime.isAfter(LocalDateTime.now())) {
                val frequency = NotificationFrequency.fromMinutes(minute)
                val userNotification =
                    UserNotification(
                        notificationFrequency = frequency,
                        notificationToken = notificationToken,
                        notificationTime = notificationDateTime.format(dateTimeFormatter),
                        initialDepartureTime = departureTime.format(dateTimeFormatter),
                        updatedDepartureTime = departureTime.format(dateTimeFormatter),
                        routeId = request.lastRouteId,
                        userId = user.id
                    )
                redisOperations.saveNotification(user.id, request.lastRouteId, frequency, userNotification)
            }
        }
    }

    fun deleteRouteNotification(
        id: Long,
        request: NotificationRequest
    ) {
        val user = userReader.read(id)
        redisOperations.deleteNotification(user.id, request.lastRouteId)
    }

    fun test(id: Long) {
        val user = userReader.read(id)
        val notificationToken = user.fcmToken
        notificationManager.sendPushNotificationForTest(notificationToken)
    }
}
