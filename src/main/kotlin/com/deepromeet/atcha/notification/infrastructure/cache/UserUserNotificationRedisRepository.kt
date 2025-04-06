package com.deepromeet.atcha.notification.infrastructure.cache

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationFrequency
import com.deepromeet.atcha.notification.domatin.UserNotificationRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserUserNotificationRedisRepository(
    private val userNotificationRedisTemplate: RedisTemplate<String, UserNotification>
) : UserNotificationRepository {
    private val duration = Duration.ofHours(12)
    private val hashOps = userNotificationRedisTemplate.opsForHash<String, UserNotification>()

    override fun save(
        userNotification: UserNotification,
        userNotificationFrequency: UserNotificationFrequency
    ) {
        hashOps.put(getKey(userNotification), userNotificationFrequency.name, userNotification)
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
