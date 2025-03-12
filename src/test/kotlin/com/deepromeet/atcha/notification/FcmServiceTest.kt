package com.deepromeet.atcha.notification

import com.deepromeet.atcha.notification.infrastructure.FcmService
import com.google.firebase.messaging.FirebaseMessaging
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class FcmServiceTest {
    @Mock
    lateinit var firebaseMessaging: FirebaseMessaging

    @InjectMocks
    lateinit var fcmService: FcmService

    @Test
    fun `FCM 메시지 전송 시 메시지 ID 반환`() {
        // given
        whenever(firebaseMessaging.send(any())).thenReturn("mockMessageId")

        // when
        val result = fcmService.sendMessageTo("dummy_token", "Test Title", "Test Body")

        // then
        assertEquals("mockMessageId", result)
    }
}
