package com.deepromeet.atcha.auth.infrastructure.provider.apple

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.stereotype.Component
import java.security.PublicKey
import java.util.Base64

@Component
class AppleTokenParser(
    private val objectMapper: ObjectMapper
) {
    companion object {
        private const val IDENTITY_TOKEN_VALUE_DELIMITER = "\\."
        private const val HEADER_INDEX = 0
    }

    fun parseHeader(appleToken: String): Map<String, String> {
        return try {
            val encodedHeader = appleToken.split(Regex(IDENTITY_TOKEN_VALUE_DELIMITER))[HEADER_INDEX]
            val decodedBytes = Base64.getUrlDecoder().decode(encodedHeader)
            val decodedHeader = String(decodedBytes)
            objectMapper.readValue(decodedHeader, Map::class.java) as Map<String, String>
        } catch (e: JsonMappingException) {
            throw RuntimeException("appleToken 값이 jwt 형식인지, 값이 정상적인지 확인해주세요.")
        } catch (e: JsonProcessingException) {
            throw RuntimeException("디코드된 헤더를 Map 형태로 분류할 수 없습니다. 헤더를 확인해주세요.")
        }
    }

    fun extractClaims(
        appleToken: String,
        publicKey: PublicKey
    ): Claims {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(publicKey) // verifyWith은 대신 setSigningKey 사용
                .build()
                .parseClaimsJws(appleToken)
                .body
        } catch (e: UnsupportedJwtException) {
            throw IllegalArgumentException("지원되지 않는 jwt 타입")
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("비어있는 jwt")
        } catch (e: JwtException) {
            throw JwtException("jwt 검증 or 분석 오류")
        }
    }
}
