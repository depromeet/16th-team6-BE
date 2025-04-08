package com.deepromeet.atcha.common.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
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
        val lockAcquire = valueOps.setIfAbsent(lockKey, lockValue, Duration.ofMillis(1000))
        if (lockAcquire == true) {
            var result = false
            try {
                log.info { "$lockKey Lock acquire = $result." }
                result = action()
            } finally {
                val script =
                    RedisScript.of<String>(
                        """
                    if redis.call("get", KEYS[1]) == ARGV[1] then
                        return redis.call("del", KEYS[1])
                    else
                        return 0
                    end
                """
                    )
                lockRedisTemplate.execute(script, listOf(lockKey), lockValue)
            }
            return result
        }
        return false
    }
}
