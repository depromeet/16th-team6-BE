package com.deepromeet.atcha.scheduler

import com.deepromeet.atcha.notification.domatin.Messaging
import com.deepromeet.atcha.notification.domatin.MessagingProvider
import com.deepromeet.atcha.notification.domatin.UserNotificationFrequency
import com.deepromeet.atcha.notification.domatin.UserNotificationRepository
import com.deepromeet.atcha.notification.infrastructure.redis.RedisStreamInitializer
import com.deepromeet.atcha.notification.infrastructure.scheduler.NotificationScheduler
import com.deepromeet.atcha.support.BaseServiceTest
import com.deepromeet.atcha.support.fixture.LastRouteFixture
import com.deepromeet.atcha.support.fixture.UserNotificationFixture
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class NotificationSchedulerTest : BaseServiceTest() {
    @Autowired
    private lateinit var notificationScheduler: NotificationScheduler

    @Autowired
    private lateinit var userNotificationRepository: UserNotificationRepository

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var redisStreamInitializer: RedisStreamInitializer

    @MockitoBean
    private lateinit var messagingProvider: MessagingProvider

    @BeforeEach
    fun initStream() {
        redisStreamInitializer.afterPropertiesSet()
        println("DONW")
    }

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

    @Test
    fun `현 시간에 보내야하는 알림을 등록한다`() {
        // given
        val lastRoute = LastRouteFixture.create()
        val notificationA =
            UserNotificationFixture.create(
                userNotificationFrequency = UserNotificationFrequency.ONE,
                routeId = lastRoute.routeId
            )
        val notificationB =
            UserNotificationFixture.create(
                userNotificationFrequency = UserNotificationFrequency.TEN,
                routeId = lastRoute.routeId
            )

        lastRouteAppender.append(lastRoute)
        userNotificationRepository.save(notificationA)
        userNotificationRepository.save(notificationB)
        notificationScheduler.checkAndSendNotifications()

        val streamKey = "stream:notification"
        val groupName = "notification-group"

        // when
        val result =
            redisTemplate.opsForStream<String, String>()
                .read(
                    Consumer.from(groupName, streamKey),
                    StreamReadOptions.empty()
                        .count(100)
                        .block(Duration.ofSeconds(1)),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
                )

        // then
        Assertions.assertThat(result).hasSize(2)
    }
}
