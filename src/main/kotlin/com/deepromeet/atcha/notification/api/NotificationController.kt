package com.deepromeet.atcha.notification.api

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.notification.api.request.NotificationRequest
import com.deepromeet.atcha.notification.domatin.UserLastRouteService
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
    private val userLastRouteService: UserLastRouteService
) {
    @PostMapping("/route")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addRouteNotification(
        @CurrentUser id: Long,
        @RequestBody request: NotificationRequest
    ) {
        userLastRouteService.addUserLastRoute(id, request.lastRouteId)
    }

    @DeleteMapping("/route")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRouteNotification(
        @CurrentUser id: Long,
        @ModelAttribute request: NotificationRequest
    ) {
        userLastRouteService.deleteUserLastRoute(id, request.lastRouteId)
    }
}
