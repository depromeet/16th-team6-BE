package com.deepromeet.seulseul.common.token

import com.deepromeet.seulseul.common.token.exception.TokenException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class TokenGenerator(
    @Value("\${jwt.access.secret}")
    private val accessSecret: String,
    @Value("\${jwt.refresh.secret}")
    private val refreshSecret: String,
) {
    private val tokenKeyMap = mapOf(
        TokenType.ACCESS to Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret)),
        TokenType.REFRESH to Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))
    )

    fun generateToken(userId: Long) : TokenInfo {
        val now = Date()
        val accessToken = generateToken(userId, now, TokenType.ACCESS)
        val refreshToken = generateToken(userId, now, TokenType.REFRESH)
        return TokenInfo(accessToken, refreshToken)
    }

    fun validateToken(token: String, tokenType: TokenType) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(tokenKeyMap.get(tokenType))
                .build()
                .parse(token)
        } catch (e: ExpiredJwtException) {
            throw TokenException.ExpiredToken
        } catch (e: Exception) {
            throw TokenException.NotValidToken
        }
    }

    fun getUserIdByToken(token: String, tokenType: TokenType) : Long {
        try {
            val body = Jwts.parserBuilder()
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

    private fun generateToken(userId: Long, now: Date, tokenType: TokenType) :String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(Date(now.time + tokenType.expirationMills))
            .signWith(tokenKeyMap.get(tokenType))
            .compact()
    }
}
