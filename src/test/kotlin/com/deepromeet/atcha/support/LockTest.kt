package com.deepromeet.atcha.support

import com.deepromeet.atcha.common.redis.LockRedisManager
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
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
        val threadCount = 2000
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

// 아래 테스트는 실행 할 떄 마다 결과가 다르기에 주석 처리합니다.

//    @Test
//    fun `Redis 락 TTL 만료로 인한 중복 실행 여부 확인`() {
//        val valueOps = lockRedisTemplate.opsForValue()
//        val lockKey = "lock:test:race"
//
//        val threadCount = 2000
//        val executor = Executors.newFixedThreadPool(threadCount)
//        val successCount = AtomicInteger(0)  // 중복 실행 여부 확인용
//        val latch = CountDownLatch(threadCount)
//
//        repeat(threadCount) {
//            executor.submit {
//                val lockValue = UUID.randomUUID().toString()
//                val lockAcquired = valueOps.setIfAbsent(lockKey, lockValue, Duration.ofMillis(3000))
//
//                if (lockAcquired == true) {
//                    // action()이라 가정
//                    val count = successCount.incrementAndGet()
//                    println("🔐 Lock acquired by thread-$it (execution #$count)")
//
//                    Thread.sleep(1000) // 일부러 TTL(1초)보다 길게 잡음
//
//                    // 락 해제 시도 - get & delete (비원자적)
//                    val currentValue = valueOps.get(lockKey)
//                    if (currentValue == lockValue) {
//                        lockRedisTemplate.delete(lockKey)
//                    }
//                }
//
//                latch.countDown()
//            }
//        }
//
//        latch.await()
//
//        // 🔍 실행된 action 횟수는 1번이어야 정상
//        Assertions.assertThat(successCount.get())
//            .withFailMessage("⚠️ 락이 중복 획득되어 작업이 %d번 실행됨", successCount.get())
//            .isEqualTo(1)
//    }
}
