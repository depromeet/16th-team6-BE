package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.notification.api.NotificationRequest
import com.deepromeet.atcha.notification.exception.NotificationException
import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import com.deepromeet.atcha.user.domain.UserReader
import java.time.LocalDateTime
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val lastRoutesResponseRedisTemplate: RedisTemplate<String, LastRoutesResponse>,
    private val redisTemplate: StringRedisTemplate,
    private val userReader: UserReader
) {
    fun addRouteNotification(
        id: Long,
        request: NotificationRequest
    ) {
        val route = getRedisLastRoute(request.lastRouteId) ?: throw NotificationException.InvalidRouteId
        val departureTime = LocalDateTime.parse(route.departureDateTime)

        val user = userReader.read(id)
        val notificationToken = request.alertToken // TODO : 사용자 정보에서 찾아오는 방향으로 결정되면 변경 필요

        user.alertFrequencies.forEach { minute ->
            val notificationTime = departureTime.minusMinutes(minute.toLong())
            val notificationMap =
                mapOf(
                    "notificationToken" to notificationToken,
                    "notificationTime" to notificationTime.toString()
                )
            addRedisNotification(id.toString(), request.lastRouteId, minute, notificationMap)
        }
    }

    fun getRedisLastRoute(routeId: String): LastRoutesResponse? {
        val key = "routes:last:$routeId"
        return lastRoutesResponseRedisTemplate.opsForValue().get(key)
    }

    fun addRedisNotification(
        userId: String,
        lastRouteId: String,
        minute: Int,
        notificationMap: Map<String, String>
    ) = redisTemplate.opsForHash<String, String>()
        .putAll("notification:$userId:$lastRouteId:$minute", notificationMap)
}
