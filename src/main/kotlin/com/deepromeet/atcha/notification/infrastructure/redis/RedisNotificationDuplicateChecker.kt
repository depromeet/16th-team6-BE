package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.application.NotificationDuplicateChecker
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisNotificationDuplicateChecker(
    private val redisTemplate: RedisTemplate<String, String>
) : NotificationDuplicateChecker {
    override fun isNewNotification(idempotencyKey: String): Boolean {
        return redisTemplate.opsForValue().setIfAbsent(
            idempotencyKey,
            System.currentTimeMillis().toString(),
            Duration.ofHours(2)
        ) ?: false
    }

    override fun markAsFailed(idempotencyKey: String) {
        redisTemplate.delete(idempotencyKey)
    }
}
