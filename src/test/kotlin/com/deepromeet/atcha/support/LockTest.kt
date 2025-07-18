package com.deepromeet.atcha.support

import com.deepromeet.atcha.shared.infrastructure.cache.config.LockRedisManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class LockTest : BaseServiceTest() {
    @Autowired
    private lateinit var redisLockRedisManager: LockRedisManager

    @Test
    fun `프로세스를 잠금하여 실행한다`() {
        val threadCount = 2000
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        repeat(threadCount) {
            executor.submit {
                try {
                    redisLockRedisManager.processWithLock(
                        "lock:test:concurrent"
                    ) {
                        Thread.sleep(2000)
                        successCount.incrementAndGet()
                        true
                    }
                } catch (e: Exception) {
                    println(e.message)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        Assertions.assertThat(successCount.get()).isEqualTo(1)
    }

    @Test
    fun `내부 작업이 오래걸리면 TTL을 연장한다`() {
        // given
        val expectedActionDuration = 500L
        val actualActionDuration = expectedActionDuration + 1000L

        // when
        val result =
            redisLockRedisManager.processWithCoroutineLock("lock:test:ttl", expectedActionDuration) {
                println(Thread.currentThread().name)
                Thread.sleep(actualActionDuration)
                true
            }

        // then
        Assertions.assertThat(result).isEqualTo(true)
    }
}
