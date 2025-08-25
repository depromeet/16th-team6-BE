package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.application.NotificationDuplicateChecker
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class RedisNotificationDuplicateChecker(
    private val redisTemplate: RedisTemplate<String, String>
) : NotificationDuplicateChecker {
    override fun isNewNotification(idempotencyKey: String): Boolean {
        return try {
            redisTemplate.opsForValue().setIfAbsent(
                idempotencyKey,
                System.currentTimeMillis().toString(),
                Duration.ofHours(2)
            ) ?: false
        } catch (e: Exception) {
            logger.warn { "알림 중복 확인 중 오류 발생: ${e.message}" }
            false
        }
    }

    override fun markAsFailed(idempotencyKey: String) {
        try {
            redisTemplate.delete(idempotencyKey)
        } catch (e: Exception) {
            logger.warn { "알림 실패 표시 중 오류 발생: ${e.message}" }
        }
    }
}
