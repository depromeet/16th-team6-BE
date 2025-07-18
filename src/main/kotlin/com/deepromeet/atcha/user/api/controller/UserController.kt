package com.deepromeet.atcha.user.api.controller

import com.deepromeet.atcha.app.application.AppService
import com.deepromeet.atcha.shared.web.ApiResponse
import com.deepromeet.atcha.shared.web.token.CurrentUser
import com.deepromeet.atcha.user.api.request.UserInfoUpdateRequest
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import com.deepromeet.atcha.user.api.response.UserInfoUpdateResponse
import com.deepromeet.atcha.user.application.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService,
    private val appService: AppService
) {
    @GetMapping("/members/me")
    fun getUserInfo(
        @CurrentUser id: Long
    ): ApiResponse<UserInfoResponse> {
        val user = userService.getUser(id)
        val appVersion = appService.getAppVersion()
        return ApiResponse.success(UserInfoResponse.from(user, appVersion))
    }

    @PutMapping("/members/me")
    fun updateUserInfo(
        @CurrentUser id: Long,
        @RequestBody userInfoUpdateRequest: UserInfoUpdateRequest
    ): ApiResponse<UserInfoUpdateResponse> {
        val result = userService.updateUser(id, userInfoUpdateRequest.toUpdateUserInfo())
        return ApiResponse.success(UserInfoUpdateResponse.from(result))
    }

    @DeleteMapping("/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @CurrentUser id: Long
    ) {
        userService.deleteUser(id)
    }
}
