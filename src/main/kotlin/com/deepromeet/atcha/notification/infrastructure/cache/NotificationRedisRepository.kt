package com.deepromeet.atcha.notification.infrastructure.cache

import com.deepromeet.atcha.notification.domatin.NotificationFrequency
import com.deepromeet.atcha.notification.domatin.NotificationRepository
import com.deepromeet.atcha.notification.domatin.UserNotification
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class NotificationRedisRepository(
    private val userNotificationRedisTemplate: RedisTemplate<String, UserNotification>
) : NotificationRepository {
    private val duration = Duration.ofHours(12)
    private val hashOps = userNotificationRedisTemplate.opsForHash<String, UserNotification>()

    override fun save(
        userNotification: UserNotification,
        notificationFrequency: NotificationFrequency
    ) {
        hashOps.put(getKey(userNotification), notificationFrequency.name, userNotification)
        hashOps.apply { userNotificationRedisTemplate.expire(getKey(userNotification), duration) }
    }

    override fun findById(
        userId: Long,
        routeId: String
    ): List<UserNotification> =
        hashOps.values(
            getKey(userId, routeId)
        )

    override fun delete(
        userId: Long,
        routeId: String
    ) {
        userNotificationRedisTemplate.delete(getKey(userId, routeId))
    }

    private fun getKey(userNotification: UserNotification) =
        "notification:${userNotification.userId}:${userNotification.lastRouteId}"

    private fun getKey(
        userId: Long,
        lastRouteId: String
    ) = "notification:$userId:$lastRouteId"
}
