package com.deepromeet.atcha.shared.web.token

import com.deepromeet.atcha.user.domain.UserId
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtTokenGenerator(
    @Value("\${jwt.access.secret}")
    private val accessSecret: String,
    @Value("\${jwt.refresh.secret}")
    private val refreshSecret: String,
    @Value("\${jwt.access.expiration}")
    private val accessExpiration: String,
    @Value("\${jwt.refresh.expiration}")
    private val refreshExpiration: String
) {
    private val tokenKeyMap =
        mapOf(
            TokenType.ACCESS to Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret)),
            TokenType.REFRESH to Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))
        )

    fun generateTokens(userId: UserId): TokenInfo {
        val now = Date()
        val accessToken = generateAccessToken(userId, now)
        val refreshToken = generateRefreshToken(userId, now, accessToken)
        return TokenInfo(accessToken, refreshToken)
    }

    private fun generateAccessToken(
        userId: UserId,
        now: Date
    ): String {
        return Jwts.builder()
            .setSubject(userId.value.toString())
            .setIssuedAt(now)
            .setId(UUID.randomUUID().toString())
            .setExpiration(Date(now.time + accessExpiration.toLong()))
            .signWith(tokenKeyMap.get(TokenType.ACCESS))
            .compact()
    }

    private fun generateRefreshToken(
        userId: UserId,
        now: Date,
        accessToken: String
    ): String {
        return Jwts.builder()
            .setSubject(userId.value.toString())
            .setIssuedAt(now)
            .claim(TokenType.ACCESS.name, accessToken)
            .setId(UUID.randomUUID().toString())
            .setExpiration(Date(now.time + refreshExpiration.toLong()))
            .signWith(tokenKeyMap.get(TokenType.REFRESH))
            .compact()
    }
}
