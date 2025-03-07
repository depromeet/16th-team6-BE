package com.deepromeet.atcha.notification

import com.deepromeet.atcha.notification.api.NotificationRequest
import com.deepromeet.atcha.notification.domatin.NotificationService
import com.deepromeet.atcha.support.fixture.UserFixture
import com.deepromeet.atcha.user.domain.UserReader
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.LocalTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {
    @Mock
    lateinit var redisTemplate: StringRedisTemplate

    @Mock
    lateinit var userReader: UserReader

    @Mock
    lateinit var hashOps: HashOperations<String, String, String>

    @InjectMocks
    lateinit var notificationService: NotificationService

    @BeforeEach
    fun setUp() {
        whenever(redisTemplate.opsForHash<String, String>()).thenReturn(hashOps)
    }

    @Test
    fun `알림 등록`() {
        // given
        val departureTime = LocalTime.parse("22:00:00")
        val user = UserFixture.create()
        val userId = user.id
        val request = NotificationRequest(lastRouteId = "route123")
        whenever(userReader.read(userId)).thenReturn(user)

        // when
        notificationService.addRouteNotification(userId, request)

        // then
        user.alertFrequencies.forEach { minute ->
            val notificationTime = departureTime.minusMinutes(minute.toLong()).toString()
            val expectedKey = "notification:$userId:${request.lastRouteId}:$minute"
            val expectedMap =
                mapOf(
                    "notificationToken" to "TOKEN",
                    "notificationTime" to notificationTime
                )
            verify(hashOps).putAll(expectedKey, expectedMap)
        }
    }
}
