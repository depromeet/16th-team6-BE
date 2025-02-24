package com.deepromeet.atcha.user.api.controller

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import com.deepromeet.atcha.user.domain.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/members/me")
    fun getUserInfo(@CurrentUser id: Long) : ApiResponse<UserInfoResponse> =
        ApiResponse.success(userService.getUserInfo(id))
}
