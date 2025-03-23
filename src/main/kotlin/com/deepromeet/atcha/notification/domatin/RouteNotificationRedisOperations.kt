package com.deepromeet.atcha.notification.domatin

import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RouteNotificationRedisOperations(
    private val lastRoutesResponseRedisTemplate: RedisTemplate<String, LastRoutesResponse>,
    private val routeNotificationRedisTemplate: RedisTemplate<String, UserNotification>
) {
    fun findLastRoute(lastRouteId: String): LastRoutesResponse? {
        val key = "routes:last:$lastRouteId"
        return lastRoutesResponseRedisTemplate.opsForValue().get(key)
    }

    fun saveNotification(
        userId: Long,
        lastRouteId: String,
        notificationFrequency: Int,
        userNotification: UserNotification
    ) = routeNotificationRedisTemplate.opsForHash<String, UserNotification>()
        .put("notification:$userId:$lastRouteId", notificationFrequency.toString(), userNotification)
        .also { routeNotificationRedisTemplate.expire("notification:$userId:$lastRouteId", Duration.ofHours(12)) }

    fun deleteNotification(
        userId: Long,
        lastRouteId: String
    ) {
        routeNotificationRedisTemplate.delete("notification:$userId:$lastRouteId")
    }

    fun findNotificationsByMinute(timeMinute: String): List<UserNotification> {
        val pattern = "notification:*"
        val notificationKeys = routeNotificationRedisTemplate.keys(pattern) // TODO: SCAN으로 변경

        return notificationKeys.flatMap { key ->
            val entries =
                routeNotificationRedisTemplate.opsForHash<String, UserNotification>()
                    .entries(key)

            entries.values.filter { notification ->
                notification.notificationTime.substring(0, 16) == timeMinute
            }
        }
    }

    fun deleteNotification(notification: UserNotification) {
        val pattern = "notification:*"
        val keys = routeNotificationRedisTemplate.keys(pattern)

        keys.forEach { key ->
            routeNotificationRedisTemplate.opsForHash<String, UserNotification>()
                .entries(key)
                .forEach { (hashKey, value) ->
                    if (value.notificationTime == notification.notificationTime) {
                        routeNotificationRedisTemplate.opsForHash<String, UserNotification>()
                            .delete(key, hashKey)
                    }
                }
        }
    }

    fun findLastRouteIdByUserId(userId: Long): String? {
        val pattern = "notification:$userId:*"
        val keys = routeNotificationRedisTemplate.keys(pattern)

        return keys.firstOrNull()?.split(":")?.get(2)
    }
}
