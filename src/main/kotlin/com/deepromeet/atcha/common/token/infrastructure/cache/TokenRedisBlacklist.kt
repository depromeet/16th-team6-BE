package com.deepromeet.atcha.common.token.infrastructure.cache

import com.deepromeet.atcha.common.token.TokenBlacklist
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class TokenRedisBlacklist(
    private val blacklistRedisTemplate: RedisTemplate<String, String>
) : TokenBlacklist {
    override fun add(token: String) {
        if (!contains(token)) {
            blacklistRedisTemplate.opsForValue()
                .set(getKey(token), token)
        }
    }

    override fun contains(token: String): Boolean = get(token) != null

    private fun get(token: String): String? =
        blacklistRedisTemplate.opsForValue()
            .get(getKey(token))

    private fun getKey(token: String): String = "BLACKLIST:$token"
}
