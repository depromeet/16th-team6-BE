package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.notification.api.NotificationRequest
import com.deepromeet.atcha.notification.exception.NotificationException
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationService(
    private val redisOperations: RouteNotificationRedisOperations,
    private val userReader: UserReader
) {
    fun addRouteNotification(
        id: Long,
        request: NotificationRequest
    ) {
        val route =
            redisOperations.findLastRoute(request.lastRouteId)
                ?: throw NotificationException.InvalidRouteId
        val departureTime = LocalDateTime.parse(route.departureDateTime)

        val user = userReader.read(id)
        val notificationToken = request.notificationToken // TODO : 사용자 정보에서 찾아오는 방향으로 결정되면 변경 필요

        user.alertFrequencies.forEach { minute ->
            val userNotification =
                UserNotification(
                    notificationToken = notificationToken,
                    notificationTime = departureTime.minusMinutes(minute.toLong()).toString()
                )
            redisOperations.saveNotification(user.id, request.lastRouteId, minute, userNotification)
        }
    }

    fun deleteRouteNotification(
        id: Long,
        request: NotificationRequest
    ) {
        val user = userReader.read(id)
        redisOperations.deleteNotification(user.id, request.lastRouteId)
    }
}
