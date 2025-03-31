package com.deepromeet.atcha.scheduler

import com.deepromeet.atcha.notification.domatin.NotificationManager
import com.deepromeet.atcha.notification.domatin.RouteNotificationRedisOperations
import com.deepromeet.atcha.notification.infrastructure.fcm.FcmService
import com.deepromeet.atcha.support.BaseServiceTest
import com.deepromeet.atcha.support.fixture.UserNotificationFixture
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class NotificationManagerTest : BaseServiceTest() {
    @Autowired
    private lateinit var notificationManager: NotificationManager

    @Autowired
    private lateinit var lockRedisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var redisOperations: RouteNotificationRedisOperations

    @MockitoBean
    private lateinit var fcmService: FcmService

    @Test
    fun `동시에 푸시 알림을 요청해도 하나만 성공한다`() {
        `when`(fcmService.sendMessageTo(anyString(), anyString(), anyString(), anyMap())).thenReturn("")
        val notification = UserNotificationFixture.create()
        val threadCount = 1000
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(threadCount)

        redisOperations.saveNotification(
            notification.userId,
            notification.routeId,
            notification.notificationFrequency,
            notification
        )
        repeat(threadCount) {
            executor.submit {
                try {
                    val success = notificationManager.sendAndDeleteNotification(notification)
                    if (success) {
                        successCount.incrementAndGet()
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
    fun `락 해제 race condition 테스트`() {
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
