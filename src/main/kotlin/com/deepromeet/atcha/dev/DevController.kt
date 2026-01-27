package com.deepromeet.atcha.dev

import com.deepromeet.atcha.shared.web.ApiResponse
import com.deepromeet.atcha.shared.web.token.JwtTokenGenerator
import com.deepromeet.atcha.user.domain.UserId
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dev")
@Profile("local", "dev")
class DevController(
    private val jwtTokenGenerator: JwtTokenGenerator
) {
    @GetMapping("/token")
    fun generateToken(
        @RequestParam("userPk") userPk: Long
    ): ApiResponse<TokenResponse> {
        val userId = UserId(userPk)
        val tokenInfo = jwtTokenGenerator.generateTokens(userId)

        val response = TokenResponse(
            userId = userId.value,
            accessToken = tokenInfo.accessToken,
            refreshToken = tokenInfo.refreshToken
        )

        return ApiResponse.success(response)
    }
}

data class TokenResponse(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String
)
