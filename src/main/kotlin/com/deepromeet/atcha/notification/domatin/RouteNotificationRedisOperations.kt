package com.deepromeet.atcha.notification.domatin

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class RouteNotificationRedisOperations(
    private val routeNotificationRedisTemplate: RedisTemplate<String, UserNotification>
) {
    private val duration = Duration.ofHours(12)
    private val hashOps = routeNotificationRedisTemplate.opsForHash<String, UserNotification>()
    private val scanOptions = ScanOptions.scanOptions().match("notification:*").count(1000).build()
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun saveNotification(
        userId: Long,
        lastRouteId: String,
        notificationFrequency: NotificationFrequency,
        userNotification: UserNotification
    ) = hashOps
        .put(getKey(userId, lastRouteId), notificationFrequency.name, userNotification)
        .also { routeNotificationRedisTemplate.expire(getKey(userId, lastRouteId), duration) }

    fun deleteNotification(
        userId: Long,
        lastRouteId: String
    ) {
        routeNotificationRedisTemplate.delete(getKey(userId, lastRouteId))
    }

    private fun getKey(
        userId: Long,
        lastRouteId: String
    ) = "notification:$userId:$lastRouteId"

    fun findNotificationsByMinute(currentMinute: String): List<UserNotification> {
        val notifications = mutableListOf<UserNotification>()
        routeNotificationRedisTemplate.scan(scanOptions).use { cursor ->
            while (cursor.hasNext()) {
                val key = cursor.next()
                val entries = hashOps.entries(key)
                notifications.addAll(
                    entries.values.filter { notification ->
                        notification.notificationTime.substring(0, 16) <= currentMinute
                    }
                )
            }
        }
        return notifications
    }

    fun deleteNotification(notification: UserNotification) {
        val key = getKey(notification.userId, notification.routeId)
        hashOps.delete(key, notification.notificationFrequency.name)
        val remaining = hashOps.entries(key)
        if (remaining.isEmpty()) {
            routeNotificationRedisTemplate.delete(key)
        }
    }

    fun processRoutes(action: (UserNotification) -> Unit) {
        routeNotificationRedisTemplate.scan(scanOptions).use { cursor ->
            while (cursor.hasNext()) {
                val keyBytes = cursor.next()
                val entries = hashOps.entries(keyBytes)
                val notification = entries.values.firstOrNull()

                notification?.let { action(it) }
            }
        }
    }

    fun findNotification(
        userId: Long,
        lastRouteId: String
    ): List<UserNotification> {
        val notifications = mutableListOf<UserNotification>()
        val scanOptions = ScanOptions.scanOptions().match(getKey(userId, lastRouteId)).build()
        routeNotificationRedisTemplate.scan(scanOptions).use { cursor ->
            while (cursor.hasNext()) {
                val key = cursor.next()
                val entries = hashOps.entries(key)
                notifications.addAll(entries.values)
            }
        }
        return notifications
    }

    fun updateDelayNotificationFlags(notification: UserNotification) {
        findNotification(notification.userId, notification.routeId).forEach { delayedNotification ->
            val updatedNotification = delayedNotification.copy(isDelayNotified = true)
            saveNotification(
                userId = delayedNotification.userId,
                lastRouteId = delayedNotification.routeId,
                notificationFrequency = delayedNotification.notificationFrequency,
                userNotification = updatedNotification
            )
        }
    }

    fun updateDepartureNotification(
        notification: UserNotification,
        newDepartureTime: LocalDateTime
    ) {
        val userId = notification.userId
        val lastRouteId = notification.routeId
        findNotification(userId, lastRouteId).forEach { delayedNotification ->
            val updatedNotification =
                delayedNotification.copy(
                    updatedDepartureTime = newDepartureTime.format(dateTimeFormatter),
                    notificationTime =
                        newDepartureTime.minusMinutes(delayedNotification.notificationFrequency.minutes)
                            .format(dateTimeFormatter)
                )

            saveNotification(
                userId = delayedNotification.userId,
                lastRouteId = delayedNotification.routeId,
                notificationFrequency = delayedNotification.notificationFrequency,
                userNotification = updatedNotification
            )
        }
    }

    fun findLastRouteIdByUserId(userId: Long): String? {
        val pattern = "notification:$userId:*"
        val keys = routeNotificationRedisTemplate.keys(pattern)

        return keys.firstOrNull()?.split(":")?.get(2)
    }
}
