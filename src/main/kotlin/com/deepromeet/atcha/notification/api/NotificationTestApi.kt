package com.deepromeet.atcha.notification.api

import com.deepromeet.atcha.notification.infrastructure.fcm.FcmService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notifications/test")
class NotificationTestApi(
    private val fcmService: FcmService
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
}
