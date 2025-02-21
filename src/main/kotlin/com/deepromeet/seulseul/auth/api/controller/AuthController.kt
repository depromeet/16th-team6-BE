package com.deepromeet.seulseul.auth.api.controller

import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.domain.OAuthService
import com.deepromeet.seulseul.auth.domain.response.ExistsUserResponse
import com.deepromeet.seulseul.auth.infrastructure.client.Provider
import com.deepromeet.seulseul.common.web.ApiResponse
import com.deepromeet.seulseul.user.api.response.UserInfoResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/api")
class AuthController(
    private val oAuthService: OAuthService
) {
    @GetMapping("/auth/check")
    fun checkUserExists(@RequestHeader("Authorization") authorizationHeader: String,
                        @RequestParam("provider") provider: Int) : ApiResponse<ExistsUserResponse> {
        log.info { "existsUser CALL" }
        val result = oAuthService.checkUserExists(authorizationHeader, Provider.findByIndex(provider))
        return ApiResponse.success(result)
    }

    @PostMapping("/auth/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@RequestHeader("Authorization") authorizationHeader: String,
               @RequestBody signUpRequest: SignUpRequest){
        oAuthService.signUp(authorizationHeader, signUpRequest)
    }

    @GetMapping("/auth/login")
    fun login(@RequestHeader("Authorization") authorizationHeader: String,
              @RequestParam("provider") provider: Int) : ApiResponse<UserInfoResponse> {
        val result = oAuthService.login(authorizationHeader, provider)
        log.info { "Login Success $result" }
        return ApiResponse.success(result)
    }

    @PostMapping("/auth/logout")
    fun logout(@RequestHeader("Authorization") authorizationHeader: String,
               @RequestParam("provider") provider: Int) {
        log.info { "logout CALL" }
        val result = oAuthService.logout(authorizationHeader)
        log.info { "result=$result" }
    }
}
