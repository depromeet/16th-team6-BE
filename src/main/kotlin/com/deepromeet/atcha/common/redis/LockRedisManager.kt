package com.deepromeet.atcha.common.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class LockRedisManager(
    private val lockRedisTemplate: RedisTemplate<String, String>
) {
    private val valueOps = lockRedisTemplate.opsForValue()
    private val log = KotlinLogging.logger {}

    fun processWithLock(
        lockKey: String,
        action: () -> Boolean
    ): Boolean {
        val lockValue = UUID.randomUUID().toString()
        val lockAcquire = valueOps.setIfAbsent(lockKey, lockValue, Duration.ofMillis(3000))
        if (lockAcquire == true) {
            var result = false
            try {
                result = action()
            } finally {
                val currentValue = valueOps.get(lockKey)

                if (currentValue == lockValue) {
                    lockRedisTemplate.delete(lockKey)
                }
            }
            log.info { "$lockKey Lock acquire = $result." }
            return result
        }

        log.info { "$lockKey Lock acquire = false." }
        return false
    }
}
