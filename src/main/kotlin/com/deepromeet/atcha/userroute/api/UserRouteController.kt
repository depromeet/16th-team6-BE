package com.deepromeet.atcha.userroute.api

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.userroute.api.request.UserRouteRequest
import com.deepromeet.atcha.userroute.domain.UserRouteService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user-routes")
class UserRouteController(
    private val userRouteService: UserRouteService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addUserRoute(
        @CurrentUser id: Long,
        @RequestBody request: UserRouteRequest
    ) {
        userRouteService.addUserRoute(id, request.lastRouteId)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUserRoute(
        @CurrentUser id: Long,
        @ModelAttribute request: UserRouteRequest
    ) {
        userRouteService.deleteUserRoute(id, request.lastRouteId)
    }
}
