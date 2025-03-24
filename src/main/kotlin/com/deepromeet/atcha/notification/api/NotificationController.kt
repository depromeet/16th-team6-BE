package com.deepromeet.atcha.notification.api

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.notification.domatin.NotificationService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    @PostMapping("/route")
    fun addRouteNotification(
        @CurrentUser id: Long,
        @RequestBody request: NotificationRequest
    ) = notificationService.addRouteNotification(id, request)

    @DeleteMapping("/route")
    fun deleteRouteNotification(
        @CurrentUser id: Long,
        @ModelAttribute request: NotificationRequest
    ) = notificationService.deleteRouteNotification(id, request)
}

data class NotificationRequest(
    val lastRouteId: String
)
