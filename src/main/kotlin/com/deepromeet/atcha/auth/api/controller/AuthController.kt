package com.deepromeet.atcha.auth.api.controller

import com.deepromeet.atcha.auth.api.request.SignUpRequest
import com.deepromeet.atcha.auth.domain.AuthService
import com.deepromeet.atcha.auth.api.response.ExistsUserResponse
import com.deepromeet.atcha.auth.domain.response.LoginResponse
import com.deepromeet.atcha.auth.domain.response.ReissueTokenResponse
import com.deepromeet.atcha.auth.api.response.SignUpResponse
import com.deepromeet.atcha.common.token.Token
import com.deepromeet.atcha.common.web.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api")
class AuthController(
    private val authService: AuthService
) {
    @GetMapping("/auth/check")
    fun checkUserExists(
        @Token providerToken: String,
        @RequestParam("provider") provider: Int
    ): ApiResponse<ExistsUserResponse> {
        val exist = authService.checkUserExists(providerToken, provider)
        val result = ExistsUserResponse(exist)
        return ApiResponse.success(result)
    }

    @PostMapping("/auth/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(
        @Token providerToken: String,
        @RequestBody signUpRequest: SignUpRequest
    ): ApiResponse<SignUpResponse> {
        val userToken = authService.signUp(providerToken, signUpRequest)
        val result = SignUpResponse(userToken)
        return ApiResponse.success(result)
    }

    @GetMapping("/auth/login")
    fun login(
        @Token providerToken: String,
        @RequestParam("provider") provider: Int
    ): ApiResponse<LoginResponse> {
        val result = authService.login(providerToken, provider)
        return ApiResponse.success(result)
    }

    @PostMapping("/auth/logout")
    fun logout(
        @Token accessToken: String
    ): ApiResponse<Unit> {
        authService.logout(accessToken)
        return ApiResponse.success()
    }

    @GetMapping("/auth/reissue")
    fun reissueToken(
        @Token refreshToken: String
    ): ApiResponse<ReissueTokenResponse> {
        val result = authService.reissueToken(refreshToken)
        return ApiResponse.success(result)
    }
}
