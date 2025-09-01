package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.transit.domain.subway.SubwayStation
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class SubwayStationRedisConfig {
    @Bean
    fun subwayStationRedisTemplate(
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, SubwayStation> {
        val kotlinModule = KotlinModule.Builder().build()
        val objectMapper =
            ObjectMapper()
                .registerModule(JavaTimeModule())
                .registerModule(kotlinModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, SubwayStation::class.java)

        val redisTemplate = RedisTemplate<String, SubwayStation>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = jsonSerializer

        return redisTemplate
    }
}
