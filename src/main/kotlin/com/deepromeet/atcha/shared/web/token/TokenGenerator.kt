package com.deepromeet.atcha.shared.web.token

import com.deepromeet.atcha.shared.web.token.exception.TokenError
import com.deepromeet.atcha.shared.web.token.exception.TokenException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class TokenGenerator(
    @Value("\${jwt.access.secret}")
    private val accessSecret: String,
    @Value("\${jwt.refresh.secret}")
    private val refreshSecret: String,
    @Value("\${jwt.access.expiration}")
    private val accessExpiration: String,
    @Value("\${jwt.refresh.expiration}")
    private val refreshExpiration: String,
    private val blacklist: TokenBlacklist
) {
    private val tokenKeyMap =
        mapOf(
            TokenType.ACCESS to Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret)),
            TokenType.REFRESH to Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))
        )

    fun generateTokens(userId: Long): TokenInfo {
        val now = Date()
        val accessToken = generateAccessToken(userId, now)
        val refreshToken = generateRefreshToken(userId, now, accessToken)
        return TokenInfo(accessToken, refreshToken)
    }

    fun validateToken(
        token: String,
        tokenType: TokenType
    ) {
        validateContainBlacklist(token)
        try {
            validateJwtFormat(tokenType, token)
        } catch (e: ExpiredJwtException) {
            throw TokenException.Companion.of(TokenError.EXPIRED_TOKEN, "만료된 토큰입니다: ${e.message}")
        } catch (e: Exception) {
            throw TokenException.Companion.of(TokenError.NOT_VALID_TOKEN, "유효하지 않은 토큰 형식입니다: ${e.message}")
        }
    }

    fun getUserIdByToken(
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

    fun expireTokensWithRefreshToken(refreshToken: String) {
        val accessToken = getAccessTokenByRefreshToken(refreshToken)
        expireToken(accessToken)
        expireToken(refreshToken)
    }

    fun expireToken(token: String) {
        blacklist.add(token)
    }

    private fun getAccessTokenByRefreshToken(refreshToken: String): String {
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

    private fun generateAccessToken(
        userId: Long,
        now: Date
    ): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setId(UUID.randomUUID().toString())
            .setExpiration(Date(now.time + accessExpiration.toLong()))
            .signWith(tokenKeyMap.get(TokenType.ACCESS))
            .compact()
    }

    private fun generateRefreshToken(
        userId: Long,
        now: Date,
        accessToken: String
    ): String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .claim(TokenType.ACCESS.name, accessToken)
            .setId(UUID.randomUUID().toString())
            .setExpiration(Date(now.time + refreshExpiration.toLong()))
            .signWith(tokenKeyMap.get(TokenType.REFRESH))
            .compact()
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

    private fun validateContainBlacklist(token: String) {
        if (blacklist.contains(token)) {
            throw TokenException.Companion.of(TokenError.EXPIRED_TOKEN, "이미 만료되거나 로그아웃된 토큰입니다")
        }
    }
}
