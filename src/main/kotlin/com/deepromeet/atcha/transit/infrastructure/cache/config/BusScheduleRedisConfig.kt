package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.fasterxml.jackson.databind.DeserializationFeature
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
class BusScheduleRedisConfig {
    @Bean
    fun busTimeTableRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, BusSchedule> {
        val kotlinModule = KotlinModule.Builder().build()
        val objectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .registerModules(kotlinModule)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, BusSchedule::class.java)

        // RedisTemplate 설정
        val redisTemplate = RedisTemplate<String, BusSchedule>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = jsonSerializer
        return redisTemplate
    }
}
