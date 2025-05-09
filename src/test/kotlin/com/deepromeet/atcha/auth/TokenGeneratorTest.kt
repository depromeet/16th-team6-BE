package com.deepromeet.atcha.auth

import com.deepromeet.atcha.common.token.TokenBlacklist
import com.deepromeet.atcha.common.token.TokenGenerator
import com.deepromeet.atcha.common.token.TokenType
import com.deepromeet.atcha.common.token.exception.TokenException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TokenGeneratorTest(
    @Autowired
    private var tokenBlacklist: TokenBlacklist
) {
    private val accessSecret = "dGVzdEFjY2Vzc1NlY3JldEtasdleVZhbHVlMTIzNDU2Nzg="
    private val refreshSecret = "dGVzdFJmZXNoU2VjcmV0S2V5asdVmFsdWUxMjM0NTY3OA=="
    private val accessExpiration = "1800000"
    private val refreshExpiration = "1800000"
    private lateinit var tokenGenerator: TokenGenerator

    @BeforeEach
    fun setUpTokenGenerator() {
        tokenGenerator =
            TokenGenerator(
                accessSecret,
                refreshSecret,
                accessExpiration,
                refreshExpiration,
                tokenBlacklist
            )
    }

    @Test
    fun `토큰 생성 테스트 - 생성된 토큰의 subject가 userId와 일치해야 한다`() {
        val userId = 100L
        val tokenInfo = tokenGenerator.generateTokens(userId)

        // 토큰이 빈 문자열이 아닌지 확인
        assert(tokenInfo.accessToken.isNotEmpty()) { "Access token should not be empty" }
        assert(tokenInfo.refreshToken.isNotEmpty()) { "Refresh token should not be empty" }

        // accessToken 확인
        val accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret))
        val accessClaims =
            Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(tokenInfo.accessToken)
                .body
        assertEquals(userId.toString(), accessClaims.subject, "Access token의 subject는 userId와 일치해야 합니다.")
        Assertions.assertThatNoException()
            .isThrownBy { tokenGenerator.validateToken(tokenInfo.accessToken, TokenType.ACCESS) }

        // refreshToken 확인
        val refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))
        val refreshClaims =
            Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(tokenInfo.refreshToken)
                .body
        assertEquals(userId.toString(), refreshClaims.subject, "Refresh token의 subject는 userId와 일치해야 합니다.")
        Assertions.assertThatNoException()
            .isThrownBy { tokenGenerator.validateToken(tokenInfo.refreshToken, TokenType.REFRESH) }
    }

    @Test
    fun `만료된 토큰 사용지 에러가 발생한다`() {
        // given
        val userId = 100L
        val tokenInfo = tokenGenerator.generateTokens(userId)

        // when
        tokenGenerator.expireToken(tokenInfo.accessToken)

        // then
        Assertions.assertThatThrownBy { tokenGenerator.validateToken(tokenInfo.accessToken, TokenType.ACCESS) }
            .isInstanceOf(TokenException.ExpiredToken::class.java)
    }
}
