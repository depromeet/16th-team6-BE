package com.deepromeet.atcha.common.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID
import kotlin.concurrent.thread

private const val SUCCESS = 1L
private const val BUFF_RATIO = 1.5
private const val WATCHDOG_SLEEP_RATIO = 2

@Component
class LockRedisManager(
    private val lockRedisTemplate: RedisTemplate<String, String>,
    private val lockReleaseScript: RedisScript<Long>,
    private val lockRefreshScript: RedisScript<Long>
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
                            lockRefreshScript,
                            listOf(lockKey),
                            lockValue,
                            ttlMillis
                        )
                    if (lockExtendResult == SUCCESS) {
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

    fun processWithCoroutineLock(
        lockKey: String,
        expectedActionDurationMillis: Long = 600L,
        action: () -> Boolean
    ): Boolean {
        val lockValue = UUID.randomUUID().toString()

        if (!acquireLock(lockKey, lockValue, expectedActionDurationMillis)) return false
        log.info { "✅$lockKey Successfully acquired lock!" }

        var watchdogJob =
            CoroutineScope(Dispatchers.Default).launch {
                watchAndRefreshTtl(lockKey, lockValue, expectedActionDurationMillis)
            }

        var result = false
        runBlocking {
            try {
                result = action()
            } finally {
                releaseLock(watchdogJob, lockKey, lockValue)
            }
        }
        return result
    }

    private fun acquireLock(
        lockKey: String,
        lockValue: String,
        expectedActionDurationMillis: Long
    ): Boolean =
        valueOps.setIfAbsent(
            lockKey,
            lockValue,
            Duration.ofMillis((expectedActionDurationMillis * BUFF_RATIO).toLong())
        ) == true

    private fun watchAndRefreshTtl(
        lockKey: String,
        lockValue: String,
        expectedActionDurationMillis: Long
    ) {
        val sleepMills = expectedActionDurationMillis / WATCHDOG_SLEEP_RATIO

        while (true) {
            Thread.sleep(sleepMills)
            val lockRefreshResult =
                lockRedisTemplate.execute(
                    lockRefreshScript,
                    listOf(lockKey),
                    lockValue,
                    (expectedActionDurationMillis * BUFF_RATIO).toLong().toString()
                )
            if (lockRefreshResult == SUCCESS) {
                log.info { "\uD83C\uDF00 $lockKey Lock extended by ${expectedActionDurationMillis}ms." }
            } else {
                log.warn { "❌$lockKey Failed to extend lock" }
            }
        }
    }

    private fun releaseLock(
        watchdogJob: Job,
        lockKey: String,
        lockValue: String
    ) {
        watchdogJob.cancel()
        lockRedisTemplate.execute(lockReleaseScript, listOf(lockKey), lockValue)
        log.info { "⭐️$lockKey Releasing lock." }
    }
}
