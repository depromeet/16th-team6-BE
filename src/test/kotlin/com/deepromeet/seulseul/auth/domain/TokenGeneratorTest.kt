package com.deepromeet.seulseul.auth.domain

import com.deepromeet.seulseul.common.token.TokenGenerator
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TokenGeneratorTest {
    private val accessSecret = "dGVzdEFjY2Vzc1NlY3JldEtasdleVZhbHVlMTIzNDU2Nzg="
    private val refreshSecret = "dGVzdFJmZXNoU2VjcmV0S2V5asdVmFsdWUxMjM0NTY3OA=="

    @Test
    fun `토큰 생성 테스트 - 생성된 토큰의 subject가 userId와 일치해야 한다`() {
        val tokenGenerator = TokenGenerator(accessSecret, refreshSecret)
        val userId = 100L
        val tokenInfo = tokenGenerator.generateToken(userId)

        // 토큰이 빈 문자열이 아닌지 확인
        assert(tokenInfo.accessToken.isNotEmpty()) { "Access token should not be empty" }
        assert(tokenInfo.refreshToken.isNotEmpty()) { "Refresh token should not be empty" }

        // accessToken 파싱하여 subject 클레임 확인
        val accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret))
        val accessClaims = Jwts.parserBuilder()
            .setSigningKey(accessKey)
            .build()
            .parseClaimsJws(tokenInfo.accessToken)
            .body
        assertEquals(userId.toString(), accessClaims.subject, "Access token의 subject는 userId와 일치해야 합니다.")

        // refreshToken 파싱하여 subject 클레임 확인
        val refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))
        val refreshClaims = Jwts.parserBuilder()
            .setSigningKey(refreshKey)
            .build()
            .parseClaimsJws(tokenInfo.refreshToken)
            .body
        assertEquals(userId.toString(), refreshClaims.subject, "Refresh token의 subject는 userId와 일치해야 합니다.")
    }
}
