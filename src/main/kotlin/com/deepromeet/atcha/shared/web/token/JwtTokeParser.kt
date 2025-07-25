package com.deepromeet.atcha.shared.web.token

import com.deepromeet.atcha.shared.web.token.exception.TokenError
import com.deepromeet.atcha.shared.web.token.exception.TokenException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtTokeParser(
    @Value("\${jwt.access.secret}")
    private val accessSecret: String,
    @Value("\${jwt.refresh.secret}")
    private val refreshSecret: String
) {
    private val tokenKeyMap =
        mapOf(
            TokenType.ACCESS to Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret)),
            TokenType.REFRESH to Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))
        )

    fun validateToken(
        token: String,
        tokenType: TokenType
    ) {
        try {
            validateJwtFormat(tokenType, token)
        } catch (e: ExpiredJwtException) {
            throw TokenException.Companion.of(TokenError.EXPIRED_TOKEN, "만료된 토큰입니다: ${e.message}")
        } catch (e: Exception) {
            throw TokenException.Companion.of(TokenError.NOT_VALID_TOKEN, "유효하지 않은 토큰 형식입니다: ${e.message}")
        }
    }

    fun getUserId(
        token: String,
        tokenType: TokenType
    ): Long {
        try {
            val body =
                Jwts.parserBuilder()
                    .setSigningKey(tokenKeyMap.get(tokenType))
                    .build()
                    .parseClaimsJws(token)
                    .body
            return body.get("sub").toString().toLong()
        } catch (e: ExpiredJwtException) {
            throw TokenException.Companion.of(TokenError.EXPIRED_TOKEN, "만료된 토큰으로 사용자 ID를 가져올 수 없습니다: ${e.message}")
        } catch (e: Exception) {
            throw TokenException.Companion.of(
                TokenError.NOT_VALID_TOKEN,
                "유효하지 않은 토큰으로 사용자 ID를 가져올 수 없습니다: ${e.message}"
            )
        }
    }

    fun getAccessToken(refreshToken: String): String {
        try {
            val body =
                Jwts.parserBuilder()
                    .setSigningKey(tokenKeyMap.get(TokenType.REFRESH))
                    .build()
                    .parseClaimsJws(refreshToken)
                    .body
            return body.get(TokenType.ACCESS.name).toString()
        } catch (e: ExpiredJwtException) {
            throw TokenException.Companion.of(
                TokenError.EXPIRED_TOKEN,
                "만료된 리프레시 토큰으로 액세스 토큰을 가져올 수 없습니다: ${e.message}"
            )
        } catch (e: Exception) {
            throw TokenException.Companion.of(
                TokenError.NOT_VALID_TOKEN,
                "유효하지 않은 리프레시 토큰으로 액세스 토큰을 가져올 수 없습니다: ${e.message}"
            )
        }
    }

    private fun validateJwtFormat(
        tokenType: TokenType,
        token: String
    ) {
        Jwts.parserBuilder()
            .setSigningKey(tokenKeyMap.get(tokenType))
            .build()
            .parseClaimsJws(token)
            .body.get("sub")
            ?: throw TokenException.Companion.of(TokenError.NOT_VALID_TOKEN, "토큰에 사용자 정보가 포함되어 있지 않습니다")
    }
}
