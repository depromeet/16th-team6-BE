package com.deepromeet.atcha.scheduler

import com.deepromeet.atcha.notification.domatin.NotificationManager
import com.deepromeet.atcha.notification.infrastructure.fcm.FcmService
import com.deepromeet.atcha.support.BaseServiceTest
import com.deepromeet.atcha.support.fixture.UserNotificationFixture
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class NotificationManagerTest : BaseServiceTest() {
    @Autowired
    private lateinit var notificationManager: NotificationManager

    @MockitoBean
    private lateinit var fcmService: FcmService

    @Test
    fun `동시에 푸시 알림을 요청해도 하나만 성공한다`() {
        `when`(fcmService.sendMessageTo(anyString(), anyString(), anyString(), anyMap())).thenReturn("")
        val notification = UserNotificationFixture.create()
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val executor = Executors.newFixedThreadPool(threadCount)

        repeat(threadCount) {
            executor.submit {
                try {
                    val success = notificationManager.sendAndDeleteNotification(notification)
                    if (success) {
                        successCount.incrementAndGet()
                        Thread.sleep(2000) // simulate holding the lock
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        Assertions.assertThat(successCount.get()).isEqualTo(1)
    }
}
