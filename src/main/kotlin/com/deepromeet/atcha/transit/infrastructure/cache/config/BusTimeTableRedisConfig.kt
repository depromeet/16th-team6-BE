package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class BusTimeTableRedisConfig {
    @Bean
    fun busTimeTableRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, BusTimeTable> {
        // 커스텀 ObjectMapper 생성
        val kotlinModule = KotlinModule.Builder().build()
        val objectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .registerModules(kotlinModule)
        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, BusTimeTable::class.java)

        // RedisTemplate 설정
        val redisTemplate = RedisTemplate<String, BusTimeTable>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = jsonSerializer
        return redisTemplate
    }
}
