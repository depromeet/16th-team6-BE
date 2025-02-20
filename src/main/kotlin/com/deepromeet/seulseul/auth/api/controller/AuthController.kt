package com.deepromeet.seulseul.auth.api.controller

import com.deepromeet.seulseul.auth.domain.OAuthService
import com.deepromeet.seulseul.auth.domain.response.ExistsUserResponse
import com.deepromeet.seulseul.common.web.ApiResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController("/api")
class AuthController(
    private val oAuthService: OAuthService
) {
    @GetMapping("/auth/login")
    fun login(@RequestHeader("Authorization") authorizationHeader: String,
              @RequestParam("provider") provider: Int
    ) {
        log.info { "login CALL" }
        val result = oAuthService.getUserInfo(authorizationHeader)
        log.info { "result=$result" }
    }

    @PostMapping("/auth/logout")
    fun logout(@RequestHeader("Authorization") authorizationHeader: String,
               @RequestParam("provider") provider: Int) {
        log.info { "logout CALL" }
        val result = oAuthService.logout(authorizationHeader)
        log.info { "result=$result" }
    }

    @GetMapping("/auth/check")
    fun checkUserExists(@RequestHeader("Authorization") authorizationHeader: String,
                   @RequestParam("provider") provider: Int) : ApiResponse<ExistsUserResponse> {
        log.info { "existsUser CALL" }
        val result = oAuthService.checkUserExists(authorizationHeader)
        return ApiResponse.success(result)
    }
}
