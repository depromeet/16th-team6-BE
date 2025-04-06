package com.deepromeet.atcha.notification.infrastructure.cache

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationFrequency
import com.deepromeet.atcha.notification.domatin.UserNotificationRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserUserNotificationRedisRepository(
    private val userNotificationRedisTemplate: RedisTemplate<String, UserNotification>
) : UserNotificationRepository {
    private val duration = Duration.ofHours(12)
    private val hashOps = userNotificationRedisTemplate.opsForHash<String, UserNotification>()
    private val scanOptions = ScanOptions.scanOptions().match("notification:*").count(1000).build()

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

    override fun findByTime(time: String): List<UserNotification> {
        val notifications = mutableListOf<UserNotification>()
        userNotificationRedisTemplate.scan(scanOptions).use { cursor ->
            while (cursor.hasNext()) {
                val key = cursor.next()
                val entries = hashOps.entries(key)
                notifications.addAll(
                    entries.values.filter { notification ->
                        notification.notificationTime.substring(0, 16) <= time
                    }
                )
            }
        }
        return notifications
    }

    override fun hasNotification(userNotification: UserNotification): Boolean =
        userNotificationRedisTemplate.hasKey(getKey(userNotification))

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
