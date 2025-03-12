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
}
