package com.deepromeet.atcha.shared.infrastructure.cache.config

import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import java.time.Duration

@Configuration
@Profile("staging", "dev", "test", "local")
@EnableRedisRepositories(basePackages = ["com.deepromeet.atcha"])
class DefaultRedisConfig(
    @Value("\${redis.host}")
    private val host: String,
    @Value("\${redis.port}")
    private val port: Int
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config =
            LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .build()
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration(host, port),
            config
        )
    }

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

    @Bean
    fun startedBusRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, BusPosition> =
        factory.create(BusPosition::class.java)

    @Bean
    fun userRouteRedisTemplate(factory: RedisTemplateFactory): RedisTemplate<String, UserRoute> =
        factory.create(UserRoute::class.java)
}
