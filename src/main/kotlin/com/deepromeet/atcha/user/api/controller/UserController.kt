package com.deepromeet.atcha.user.api.controller

import com.deepromeet.atcha.app.application.AppService
import com.deepromeet.atcha.app.domain.Platform
import com.deepromeet.atcha.shared.web.ApiResponse
import com.deepromeet.atcha.shared.web.token.CurrentUser
import com.deepromeet.atcha.user.api.request.HomeAddressUpdateRequest
import com.deepromeet.atcha.user.api.request.UserInfoUpdateRequest
import com.deepromeet.atcha.user.api.request.UserWithdrawalRequest
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import com.deepromeet.atcha.user.api.response.UserInfoUpdateResponse
import com.deepromeet.atcha.user.application.UserService
import com.deepromeet.atcha.user.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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
        @CurrentUser userId: Long,
        @RequestHeader("X-Platform") platform: String
    ): ApiResponse<UserInfoResponse> {
        val user = userService.getUser(UserId(userId))
        val appVersion = appService.getAppVersion(Platform.fromString(platform))
        return ApiResponse.success(UserInfoResponse.from(user, appVersion))
    }

    @PutMapping("/members/me")
    fun updateUserInfo(
        @CurrentUser userId: Long,
        @RequestBody userInfoUpdateRequest: UserInfoUpdateRequest
    ): ApiResponse<UserInfoUpdateResponse> {
        val result = userService.updateUser(UserId(userId), userInfoUpdateRequest.toUpdateUserInfo())
        return ApiResponse.success(UserInfoUpdateResponse.from(result))
    }

    @PatchMapping("/members/me/home-address")
    fun updateHomeAddress(
        @CurrentUser userId: Long,
        @RequestBody request: HomeAddressUpdateRequest
    ): ApiResponse<UserInfoUpdateResponse> {
        val result = userService.updateHomeAddress(UserId(userId), request.toHomeAddress())
        return ApiResponse.success(UserInfoUpdateResponse.from(result))
    }

    @DeleteMapping("/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @CurrentUser userId: Long,
        @RequestBody request: UserWithdrawalRequest
    ) {
        userService.deleteUser(request.toDomain(UserId(userId)))
    }
}
