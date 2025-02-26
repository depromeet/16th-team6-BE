package com.deepromeet.atcha.auth.api.controller

import com.deepromeet.atcha.auth.api.request.SignUpRequest
import com.deepromeet.atcha.auth.domain.AuthService
import com.deepromeet.atcha.auth.domain.response.ExistsUserResponse
import com.deepromeet.atcha.auth.domain.response.LoginResponse
import com.deepromeet.atcha.auth.domain.response.ReissueTokenResponse
import com.deepromeet.atcha.auth.domain.response.SignUpResponse
import com.deepromeet.atcha.auth.infrastructure.client.Provider
import com.deepromeet.atcha.common.token.Token
import com.deepromeet.atcha.common.web.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api")
class AuthController(
    private val authService: AuthService
) {
    @GetMapping("/auth/check")
    fun checkUserExists(@RequestHeader("Authorization") authorizationHeader: String,
                        @RequestParam("provider") provider: Int) : ApiResponse<ExistsUserResponse> {
        val result = authService.checkUserExists(authorizationHeader, Provider.findByOrdinal(provider))
        return ApiResponse.success(result)
    }

    @PostMapping("/auth/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@RequestHeader("Authorization") authorizationHeader: String,
               @RequestBody signUpRequest: SignUpRequest) : ApiResponse<SignUpResponse> {
        val result = authService.signUp(authorizationHeader, signUpRequest)
        return ApiResponse.success(result);
    }

    @GetMapping("/auth/login")
    fun login(@RequestHeader("Authorization") authorizationHeader: String,
              @RequestParam("provider") provider: Int) : ApiResponse<LoginResponse> {
        val result = authService.login(authorizationHeader, provider)
        return ApiResponse.success(result)
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@Token accessToken: String) = authService.logout(accessToken)

    @GetMapping("/auth/reissue")
    fun reissueToken(@Token refreshToken: String) : ApiResponse<ReissueTokenResponse> {
        val result = authService.reissueToken(refreshToken)
        return ApiResponse.success(result)
    }
}
