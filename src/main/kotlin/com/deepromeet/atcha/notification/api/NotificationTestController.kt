package com.deepromeet.atcha.notification.api

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.notification.domatin.MessagingManager
import com.deepromeet.atcha.notification.domatin.NotificationContentManager
import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.notification.domatin.UserNotificationFrequency
import com.deepromeet.atcha.notification.infrastructure.redis.RedisStreamProducer
import com.deepromeet.atcha.user.domain.UserReader
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/t/notifications")
class NotificationTestController(
    private val userReader: UserReader,
    private val notificationContentManager: NotificationContentManager,
    private val messagingManager: MessagingManager,
    private val redisStreamProducer: RedisStreamProducer
) {
    // todo 안드 테스트용 (추후 삭제_
    @PostMapping("/push/{type}")
    fun testNotification(
        @CurrentUser id: Long,
        @PathVariable type: String
    ) {
        val token = userReader.read(id).fcmToken
        val suggestNotification = notificationContentManager.createTestNotification(type)
        log.info {
            "Sending push notification to" +
                " $token with body: ${suggestNotification.body} and data: ${suggestNotification.dataMap}"
        }
        messagingManager.send(suggestNotification, token)
    }

    @PostMapping("/scheduler")
    fun testSchedulerNotification(
        @CurrentUser id: Long
    ) {
        val user = userReader.read(id)
        val userNotification =
            UserNotification(
                UserNotificationFrequency.ONE,
                user.fcmToken,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(1),
                "test-id",
                user.id
            )
        redisStreamProducer.produce(userNotification)
    }
}
