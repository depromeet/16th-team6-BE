package com.deepromeet.atcha.common.token.infrastructure.cache

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class TokenRedisConfig(
    private val redisConnectionFactory: RedisConnectionFactory
) {
    @Bean
    fun blacklistRedisTemplate(): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
        }
    }
}
