package com.deepromeet.atcha.notification.domatin

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val log = KotlinLogging.logger {}

@Component
class RouteNotificationRedisOperations(
    private val routeNotificationRedisTemplate: RedisTemplate<String, UserNotification>,
    private val lockRedisTemplate: RedisTemplate<String, String>
) {
    private val duration = Duration.ofHours(12)
    private val hashOps = routeNotificationRedisTemplate.opsForHash<String, UserNotification>()
    private val lockValueOps = lockRedisTemplate.opsForValue()
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

//    fun deleteNotification(
//        userId: Long,
//        lastRouteId: String
//    ) {
//        routeNotificationRedisTemplate.delete(getKey(userId, lastRouteId))
//    }

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

    fun hasNotification(userNotification: UserNotification): Boolean =
        routeNotificationRedisTemplate.hasKey(
            getKey(userNotification.userId, userNotification.lastRouteId)
        )

    fun handleNotificationWithLock(
        userNotification: UserNotification,
        action: (UserNotification) -> Boolean
    ): Boolean {
        val lockKey =
            getLockKey(userNotification.userId, userNotification.lastRouteId, userNotification.notificationFrequency)
        val lockValue = UUID.randomUUID().toString()
        val lockAcquire = lockValueOps.setIfAbsent(lockKey, lockValue, Duration.ofMillis(3000))
        if (lockAcquire == true) {
            log.info { "$lockKey Lock 획득에 성공." }
            var result = false
            try {
                result = action(userNotification)
            } finally {
                val currentValue = lockValueOps.get(lockKey)

                if (currentValue == lockValue) {
                    lockRedisTemplate.delete(lockKey)
                }
            }
            return result
        } else {
            log.warn { "$lockKey Lock 획득에 실패했습니다." }
            return false
        }
    }

    fun updateDelayNotificationFlags(notification: UserNotification) {
        findNotification(notification.userId, notification.lastRouteId).forEach { delayedNotification ->
            val updatedNotification = delayedNotification.copy(isDelayNotified = true)
            saveNotification(
                userId = delayedNotification.userId,
                lastRouteId = delayedNotification.lastRouteId,
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
        val lastRouteId = notification.lastRouteId
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
                lastRouteId = delayedNotification.lastRouteId,
                notificationFrequency = delayedNotification.notificationFrequency,
                userNotification = updatedNotification
            )
        }
    }

    private fun getKey(
        userId: Long,
        lastRouteId: String
    ) = "notification:$userId:$lastRouteId"

    private fun getLockKey(
        userId: Long,
        lastRouteId: String,
        frequency: NotificationFrequency
    ) = "lock:notification:$userId:$lastRouteId:$frequency"
}
