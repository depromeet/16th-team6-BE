package com.deepromeet.atcha.auth.api.controller

import com.deepromeet.atcha.auth.api.request.SignUpRequest
import com.deepromeet.atcha.auth.api.response.ExistsUserResponse
import com.deepromeet.atcha.auth.api.response.LoginResponse
import com.deepromeet.atcha.auth.api.response.ReissueTokenResponse
import com.deepromeet.atcha.auth.api.response.SignUpResponse
import com.deepromeet.atcha.auth.domain.AuthService
import com.deepromeet.atcha.common.token.Token
import com.deepromeet.atcha.common.web.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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
        val signUpInfo = signUpRequest.toSignUpInfo()
        val userToken = authService.signUp(providerToken, signUpInfo)
        val result = SignUpResponse(userToken)
        return ApiResponse.success(result)
    }

    @GetMapping("/auth/login")
    fun login(
        @Token providerToken: String,
        @RequestParam("provider") provider: Int
    ): ApiResponse<LoginResponse> {
        val userToken = authService.login(providerToken, provider)
        val result = LoginResponse(userToken)
        return ApiResponse.success(result)
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @Token accessToken: String
    ) {
        authService.logout(accessToken)
    }

    @GetMapping("/auth/reissue")
    fun reissueToken(
        @Token refreshToken: String
    ): ApiResponse<ReissueTokenResponse> {
        val userToken = authService.reissueToken(refreshToken)
        val result = ReissueTokenResponse(userToken)
        return ApiResponse.success(result)
    }
}
