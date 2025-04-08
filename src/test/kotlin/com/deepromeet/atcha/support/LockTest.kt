package com.deepromeet.atcha.support

import com.deepromeet.atcha.common.redis.LockRedisManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class LockTest : BaseServiceTest() {
    @Autowired
    private lateinit var lockRedisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var redisLockRedisManager: LockRedisManager

    @Test
    fun `프로세스를 잠금하여 실행한다`() {
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        repeat(threadCount) {
            executor.submit {
                try {
                    redisLockRedisManager.processWithLock(
                        "lock:test"
                    ) {
                        successCount.incrementAndGet()
                        true
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        Assertions.assertThat(successCount.get()).isEqualTo(1)
    }

    @Test
    fun `Redis 락 해제 race condition 테스트`() {
        val valueOps = lockRedisTemplate.opsForValue()
        val lockKey = "lock:test:race"
        val lockValueA = "UUID-A"
        val lockValueB = "UUID-B"

        valueOps.setIfAbsent(lockKey, lockValueA, Duration.ofMillis(1000))

        val threadA =
            Thread {
                println("현재 락 주인: ${valueOps.get(lockKey)}")
                // lock 만료
                Thread.sleep(3000)
                println("현재 락 주인: ${valueOps.get(lockKey)}")
            }
        val threadB =
            Thread {
                Thread.sleep(1500)
                val success = valueOps.setIfAbsent(lockKey, lockValueB, Duration.ofMillis(2000))
                println("락 획득 : $success 현재 락 주인: ${valueOps.get(lockKey)}")
            }

        threadA.start()
        threadB.start()

        threadA.join()
        threadB.join()

        Assertions.assertThat(valueOps.get(lockKey)).isEqualTo(lockValueB)
    }
}
