package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.LocalDate

@Configuration
class HolidayRedisConfig {
    @Bean
    fun holidayRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, List<LocalDate>> {
        // 커스텀 ObjectMapper 생성
        val objectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        // RedisTemplate 설정
        val redisTemplate = RedisTemplate<String, List<LocalDate>>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = jsonSerializer
        redisTemplate.hashKeySerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = jsonSerializer

        return redisTemplate
    }
}
