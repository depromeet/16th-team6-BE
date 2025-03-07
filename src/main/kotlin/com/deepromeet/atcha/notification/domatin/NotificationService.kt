package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.notification.api.NotificationRequest
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.threeten.bp.LocalTime

@Service
class NotificationService(
    private val redisTemplate: StringRedisTemplate,
    private val userReader: UserReader
) {
    fun addRouteNotification(
        id: Long,
        request: NotificationRequest
    ) {
        val departureTime = LocalTime.parse("22:00:00") // TODO : 향후 막차 경로에서 출발 시간 가져올 예정

        val user = userReader.read(id)
        val notificationToken = "TOKEN" // TODO : 향후 User 정보에서 FCM Token 가져올 예정

        user.alertFrequencies.forEach { minute ->
            val notificationTime = departureTime.minusMinutes(minute.toLong())
            val notificationMap =
                mapOf(
                    "notificationToken" to notificationToken,
                    "notificationTime" to notificationTime.toString()
                )

            redisTemplate.opsForHash<String, String>()
                .putAll(redisNotification(id.toString(), request.lastRouteId, minute), notificationMap)
        }
    }

    fun redisNotification(
        userId: String,
        lastRouteId: String,
        minute: Int
    ) = "notification:$userId:$lastRouteId:$minute"
}
