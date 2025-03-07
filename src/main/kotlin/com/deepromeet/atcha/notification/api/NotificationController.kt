package com.deepromeet.atcha.notification.api

import com.deepromeet.atcha.notification.domatin.NotificationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val USER_ID = 1L; // TODO : 향후 JWT 토큰에서 추출할 예정

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    @PostMapping("/route")
    fun addRouteNotification(
//        @CurrentUser id: Long,
        @RequestBody request: NotificationRequest
    ) = notificationService.addRouteNotification(USER_ID, request)
}

data class NotificationRequest(
    val lastRouteId: String
)
