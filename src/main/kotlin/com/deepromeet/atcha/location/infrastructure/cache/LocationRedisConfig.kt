package com.deepromeet.atcha.location.infrastructure.cache

import com.deepromeet.atcha.location.domain.POI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class LocationRedisConfig(
    private val redisConnectionFactory: RedisConnectionFactory
) {
    @Bean
    fun poiHistoryRedisTemplate(): RedisTemplate<String, POI> {
        return RedisTemplate<String, POI>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = Jackson2JsonRedisSerializer(POI::class.java)
        }
    }
}
