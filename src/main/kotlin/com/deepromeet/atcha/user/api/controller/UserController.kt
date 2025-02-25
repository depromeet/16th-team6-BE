package com.deepromeet.atcha.user.api.controller

import com.deepromeet.atcha.common.token.CurrentUser
import com.deepromeet.atcha.common.web.ApiResponse
import com.deepromeet.atcha.user.api.request.UserInfoUpdateRequest
import com.deepromeet.atcha.user.api.response.UserInfoResponse
import com.deepromeet.atcha.user.domain.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/members/me")
    fun getUserInfo(@CurrentUser id: Long) : ApiResponse<UserInfoResponse> =
        ApiResponse.success(userService.getUserInfo(id))

    @PutMapping("/members/me")
    fun updateUserInfo(@CurrentUser id: Long,
                       @RequestBody userInfoUpdateRequest: UserInfoUpdateRequest) : ApiResponse<UserInfoResponse> =
        ApiResponse.success(userService.updateUserInfo(id, userInfoUpdateRequest))

    @DeleteMapping("/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@CurrentUser id: Long) {
        userService.deleteUser(id)
    }
}
