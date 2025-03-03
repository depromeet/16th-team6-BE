package com.deepromeet.atcha.common.token

import com.deepromeet.atcha.common.token.exception.TokenException
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
    private val refreshSecret: String
) {
    companion object {
        private val blackList: MutableSet<String> = mutableSetOf() // todo 만료 시간 이후 리스트에서 지우기
    }

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
            throw TokenException.ExpiredToken
        } catch (e: Exception) {
            throw TokenException.NotValidToken
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
            throw TokenException.ExpiredToken
        } catch (e: Exception) {
            throw TokenException.NotValidToken
        }
    }

    fun expireTokensWithRefreshToken(refreshToken: String) {
        val accessToken = getAccessTokenByRefreshToken(refreshToken)
        expireToken(accessToken)
        expireToken(refreshToken)
    }

    fun expireToken(token: String) {
        blackList.add(token)
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
            throw TokenException.ExpiredToken
        } catch (e: Exception) {
            throw TokenException.NotValidToken
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
            .setExpiration(Date(now.time + TokenType.ACCESS.expirationMills))
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
            .setExpiration(Date(now.time + TokenType.REFRESH.expirationMills))
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
            ?: throw TokenException.NotValidToken
    }

    private fun validateContainBlacklist(token: String) {
        if (blackList.contains(token)) {
            throw TokenException.ExpiredToken
        }
    }
}
