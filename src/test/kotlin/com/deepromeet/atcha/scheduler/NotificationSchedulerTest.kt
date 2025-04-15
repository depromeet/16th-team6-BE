package com.deepromeet.atcha.scheduler

import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingProvider
import com.deepromeet.atcha.notification.domatin.UserNotificationFrequency
import com.deepromeet.atcha.notification.domatin.UserNotificationRepository
import com.deepromeet.atcha.notification.infrastructure.scheduler.NotificationScheduler
import com.deepromeet.atcha.support.BaseServiceTest
import com.deepromeet.atcha.support.fixture.UserNotificationFixture
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class NotificationSchedulerTest : BaseServiceTest() {
    @Autowired
    private lateinit var notificationScheduler: NotificationScheduler

    @Autowired
    private lateinit var userNotificationRepository: UserNotificationRepository

    @MockitoBean
    private lateinit var messagingProvider: MessagingProvider

    @Test
    fun `동시에 푸시 알림을 요청해도 하나만 성공한다`() {
        `when`(messagingProvider.send(any<Messaging>())).thenReturn("")
        val notification = UserNotificationFixture.create(userNotificationFrequency = UserNotificationFrequency.ONE)
        val threadCount = 2
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        userNotificationRepository.save(notification)
        repeat(threadCount) {
            executor.submit {
                try {
                    notificationScheduler.checkAndSendNotifications()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
    }
}
