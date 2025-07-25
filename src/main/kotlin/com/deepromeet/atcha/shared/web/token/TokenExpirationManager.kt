package com.deepromeet.atcha.shared.web.token

import com.deepromeet.atcha.shared.web.token.exception.TokenError
import com.deepromeet.atcha.shared.web.token.exception.TokenException
import org.springframework.stereotype.Component

@Component
class TokenExpirationManager(
    private val blacklist: TokenBlacklist,
    private val jwtTokeParser: JwtTokeParser
) {
    fun expireTokensWithRefreshToken(refreshToken: String) {
        val accessToken = jwtTokeParser.getAccessToken(refreshToken)
        expireToken(accessToken)
        expireToken(refreshToken)
    }

    fun expireToken(token: String) {
        blacklist.add(token)
    }

    fun validateNotExpired(token: String) {
        if (blacklist.contains(token)) {
            throw TokenException.Companion.of(TokenError.EXPIRED_TOKEN, "이미 만료되거나 로그아웃된 토큰입니다")
        }
    }
}
