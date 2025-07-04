package com.deepromeet.atcha.notification.api

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.notification.api.request.NotificationRequest
import com.deepromeet.atcha.notification.api.request.SuggestNotificationRequest
import com.deepromeet.atcha.notification.domatin.NotificationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    @PostMapping("/route")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addRouteNotification(
        @CurrentUser id: Long,
        @RequestBody request: NotificationRequest
    ) {
        notificationService.addRouteNotification(id, request.lastRouteId)
    }

    @DeleteMapping("/route")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRouteNotification(
        @CurrentUser id: Long,
        @ModelAttribute request: NotificationRequest
    ) {
        notificationService.deleteRouteNotification(id, request.lastRouteId)
    }

    @PostMapping("/suggest")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun suggestRouteNotification(
        @CurrentUser id: Long,
        @RequestBody request: SuggestNotificationRequest
    ) {
        notificationService.suggestRouteNotification(id, request.toCoordinate())
    }
}
