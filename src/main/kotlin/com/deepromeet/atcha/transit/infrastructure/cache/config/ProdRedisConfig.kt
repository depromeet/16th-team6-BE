package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.notification.domatin.UserNotification
import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.LastRoute
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@Profile("prod")
@EnableRedisRepositories(basePackages = ["com.deepromeet.atcha"])
class ProdRedisConfig(
    @Value("\${redis.host}")
    private val host: String,
    @Value("\${redis.port}")
    private val port: Int,
    @Value("\${redis.password}")
    private val password: String
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(host, port)
        redisStandaloneConfiguration.password = RedisPassword.of(password)
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    // TODO 공통 부분 분리
    @Bean
    fun lockReleaseScript(): RedisScript<Long> {
        val script =
            """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
            """.trimIndent()
        return RedisScript.of(script, Long::class.java)
    }

    @Bean
    fun lockRefreshScript(): RedisScript<Long> {
        val script =
            """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("pexpire", KEYS[1], tonumber(ARGV[2]))
            else
                return 0
            end
            """.trimIndent()
        return RedisScript.of(script, Long::class.java)
    }

    fun <T> createRedisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
        clazz: Class<T>
    ): RedisTemplate<String, T> {
        val template = RedisTemplate<String, T>()
        template.connectionFactory = redisConnectionFactory

        val kotlinModule = KotlinModule.Builder().build()
        val objectMapper =
            ObjectMapper()
                .registerModule(kotlinModule)
                .registerModule(JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        val serializer = Jackson2JsonRedisSerializer(objectMapper, clazz)

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = serializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = serializer

        return template
    }

    @Bean
    fun lastRoutesResponseRedisTemplate(
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, LastRoute> {
        return createRedisTemplate(redisConnectionFactory, LastRoute::class.java)
    }

    @Bean
    fun startedBusRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, BusPosition> {
        return createRedisTemplate(redisConnectionFactory, BusPosition::class.java)
    }

    @Bean
    fun routeNotificationRedisTemplate(
        redisConnectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, UserNotification> {
        return createRedisTemplate(redisConnectionFactory, UserNotification::class.java)
    }
}
