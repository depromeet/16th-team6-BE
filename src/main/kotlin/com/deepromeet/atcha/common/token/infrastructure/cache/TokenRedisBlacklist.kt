package com.deepromeet.atcha.common.token.infrastructure.cache

import com.deepromeet.atcha.common.token.TokenBlacklist
import com.deepromeet.atcha.common.token.TokenType
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class TokenRedisBlacklist(
    private val blacklistRedisTemplate: RedisTemplate<String, String>
) : TokenBlacklist {
    override fun add(
        token: String,
        tokenType: TokenType
    ) {
        if (!contains(token)) {
            blacklistRedisTemplate.opsForValue()
                .set(getKey(token), token, Duration.ofMillis(tokenType.expirationMills))
        }
    }

    override fun contains(token: String): Boolean = get(token) != null

    private fun get(token: String): String? =
        blacklistRedisTemplate.opsForValue()
            .get(getKey(token))

    private fun getKey(token: String): String = "BLACKLIST:$token"
}
