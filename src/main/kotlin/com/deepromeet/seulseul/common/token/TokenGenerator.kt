package com.deepromeet.seulseul.common.token

import com.deepromeet.seulseul.common.token.exception.TokenException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class TokenGenerator(
    @Value("\${jwt.access.secret}")
    private val accessSecret: String,
    @Value("\${jwt.refresh.secret}")
    private val refreshSecret: String,
) {
    private val ACCESS_EXPIRATION_MILLISECONDS: Long = 1000L * 60 * 30 // 1시간
    private val REFRESH_EXPIRATION_MILLISECONDS: Long = 1000L * 60 * 60 * 24 * 30 // 30일
    private val accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret))
    private val refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))

    fun generateToken(userId: Long) : TokenInfo {
        val now = Date()
        val accessExpiration = Date(now.time + ACCESS_EXPIRATION_MILLISECONDS)
        val refreshExpiration = Date(now.time + REFRESH_EXPIRATION_MILLISECONDS)

        val accessToken = generateToken(userId, now, accessExpiration , accessKey)
        val refreshToken = generateToken(userId, now, refreshExpiration , refreshKey)
        return TokenInfo(accessToken, refreshToken)
    }

    fun validateAccessToken(token: String) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parse(token)
        } catch (e: ExpiredJwtException) {
            throw TokenException.ExpiredToken
        } catch (e: Exception) {
            throw TokenException.NotValidToken
        }
    }

    fun getUserIdByToken(token: String) : Long {
        try {
            val body = Jwts.parserBuilder()
                .setSigningKey(accessKey)
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

    private fun generateToken(userId: Long, now: Date, expiration: Date, secretKey: SecretKey) :String {
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact()
    }
}
