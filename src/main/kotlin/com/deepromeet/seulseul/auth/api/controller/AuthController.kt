package com.deepromeet.seulseul.auth.api.controller

import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.domain.AuthService
import com.deepromeet.seulseul.auth.domain.response.ExistsUserResponse
import com.deepromeet.seulseul.auth.domain.response.LoginResponse
import com.deepromeet.seulseul.auth.domain.response.SignUpResponse
import com.deepromeet.seulseul.auth.infrastructure.client.Provider
import com.deepromeet.seulseul.common.token.CurrentUser
import com.deepromeet.seulseul.common.web.ApiResponse
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
               @RequestBody signUpRequest: SignUpRequest,
               response: HttpServletResponse) : ApiResponse<SignUpResponse> {
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
    fun logout(@CurrentUser id: Long) {
//        val result = authService.logout(id)
//        log.info { "result=$result" }
    }

    @GetMapping("/auth/reissue")
    fun reissueToken(@RequestHeader("Authorization") refreshToken: String) {
        authService.reissueToken(refreshToken.substring("Bearer ".length))
    }
}
