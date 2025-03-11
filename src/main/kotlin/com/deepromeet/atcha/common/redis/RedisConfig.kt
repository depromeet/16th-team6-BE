package com.deepromeet.atcha.common.redis

import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableRedisRepositories(basePackages = ["com.deepromeet.atcha"])
class RedisConfig(
    @Value("\${redis.host}")
    private val host: String,
    @Value("\${redis.port}")
    private val port: Int
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(host, port)
    }

    @Bean
    fun lastRoutesResponseRedisTemplate(
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, LastRoutesResponse> {
        val template = RedisTemplate<String, LastRoutesResponse>()
        template.connectionFactory = redisConnectionFactory

        template.keySerializer = StringRedisSerializer()
        val serializer = Jackson2JsonRedisSerializer(LastRoutesResponse::class.java)

        template.valueSerializer = serializer
        return template
    }
}
