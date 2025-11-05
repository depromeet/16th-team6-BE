package com.deepromeet.atcha.notification.api

import com.deepromeet.atcha.notification.application.MessagingManager
import com.deepromeet.atcha.notification.domain.Messaging
import com.deepromeet.atcha.notification.domain.NotificationContentCreator
import com.deepromeet.atcha.notification.domain.RouteRefreshNotificationData
import com.deepromeet.atcha.notification.infrastructure.fcm.FcmService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@RestController
@RequestMapping("/notifications/test")
class NotificationTestApi(
    private val fcmService: FcmService,
    private val notificationContentCreator: NotificationContentCreator,
    private val massagingManager: MessagingManager
) {
    @PostMapping("/push")
    fun pushNotificationTest(
        @RequestParam token: String
    ) {
        fcmService.send(
            token,
            "푸시가 잘 가나요?",
            "푸시야 잘 가라",
            mapOf(Pair("test", "test"))
        )
    }

    @PostMapping("/non-push")
    fun nonPushNotificationTest(
        @RequestParam token: String
    ) {
        fcmService.sendNonPush(
            token,
            mapOf(Pair("test", "test"))
        )
    }

    @PostMapping("/silent-push")
    fun silentNotificationTest(
        @RequestParam token: String
    ) {
        fcmService.sendSilentPush(
            token,
            mapOf(Pair("test", "test"))
        )
    }

    @PostMapping("/mock-refresh")
    fun mockRefreshNotification(
        @RequestParam token: String
    ) {
        val now = LocalDateTime.now().plusMinutes(2).truncatedTo(ChronoUnit.SECONDS).toString()
        val routeRefreshNotificationData =
            RouteRefreshNotificationData(
                "1",
                token,
                UUID.randomUUID().toString(),
                now,
                now
            )

        val createPushContent = notificationContentCreator.createPushContent(routeRefreshNotificationData)
        val messaging = Messaging(createPushContent, token)
        massagingManager.send(messaging)
    }
}
