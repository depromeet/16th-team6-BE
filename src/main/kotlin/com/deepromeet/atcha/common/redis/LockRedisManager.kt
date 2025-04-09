package com.deepromeet.atcha.common.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID
import kotlin.concurrent.thread

@Component
class LockRedisManager(
    private val lockRedisTemplate: RedisTemplate<String, String>,
    private val lockReleaseScript: RedisScript<Long>,
    private val lockExtendScript: RedisScript<Long>
) {
    private val valueOps = lockRedisTemplate.opsForValue()
    private val log = KotlinLogging.logger {}

    fun processWithLock(
        lockKey: String,
        action: () -> Boolean
    ): Boolean {
        val lockValue = UUID.randomUUID().toString()
        val ttl = Duration.ofMillis(600)

        val lockAcquire = valueOps.setIfAbsent(lockKey, lockValue, ttl)
        if (lockAcquire == false) {
            return false
        }
        log.info { "✅$lockKey Successfully acquired lock!" }

        var result = false
        var keepExtending = true

        val watchdogThread =
            thread(start = true, isDaemon = true) {
                while (keepExtending) {
                    Thread.sleep(500)
                    val ttlMillis = ttl.toMillis().toString()

                    val lockExtendResult =
                        lockRedisTemplate.execute(
                            lockExtendScript,
                            listOf(lockKey),
                            lockValue,
                            ttlMillis
                        )
                    if (lockExtendResult == 1L) {
                        log.info { "\uD83C\uDF00 $lockKey Lock extended by ${ttlMillis}ms." }
                        continue
                    }
                    log.warn { "❌$lockKey Failed to extend lock" }
                    keepExtending = false
                }
            }

        try {
            result = action()
        } finally {
            keepExtending = false
            watchdogThread.join()
            lockRedisTemplate.execute(lockReleaseScript, listOf(lockKey), lockValue)
            log.info { "⭐️$lockKey Releasing lock." }
        }
        return result
    }
}
