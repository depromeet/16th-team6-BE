package com.deepromeet.seulseul.user.api.controller

import com.deepromeet.seulseul.common.web.ApiResponse
import com.deepromeet.seulseul.user.api.response.UserInfoResponse
import com.deepromeet.seulseul.user.domain.Email
import com.deepromeet.seulseul.user.domain.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService
) {

    @GetMapping("/users")
    fun getUser(email: String): ApiResponse<UserInfoResponse> {
        val user = userService.getUser(Email.from(email))
        return ApiResponse.success(UserInfoResponse.from(user))
    }
}