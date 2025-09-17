package com.deepromeet.atcha.transit.infrastructure.cache.config

import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusRouteStationList
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class ApiCacheConfig(private val redisConnectionFactory: RedisConnectionFactory) {
    private val objectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

    @Bean
    fun apiCacheManager(): CacheManager {
        val busRouteStationListSerializer = createSerializer<BusRouteStationList>()
        val busRouteListSerializer =
            createSerializer<List<BusRoute>>(
                objectMapper.typeFactory.constructType(object : TypeReference<List<BusRoute>>() {})
            )
        val busRouteInfoSerializer = createSerializer<BusRouteInfo>()

        val cacheConfigurations =
            buildMap {
                putAll(
                    createCacheConfigurationsForKeys(
                        keys = CacheKeys.Api.BUS_ROUTE_STATION_LISTS,
                        ttl = Duration.ofDays(7),
                        serializer = busRouteStationListSerializer
                    )
                )

                putAll(
                    createCacheConfigurationsForKeys(
                        keys = CacheKeys.Api.BUS_ROUTE_LISTS,
                        ttl = Duration.ofDays(7),
                        serializer = busRouteListSerializer
                    )
                )

                put(
                    CacheKeys.Transit.BUS_ROUTE_INFO,
                    createCacheConfiguration(
                        Duration.ofDays(7),
                        busRouteInfoSerializer
                    )
                )
            }

        return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    private inline fun <reified T> createSerializer(javaType: JavaType? = null): Jackson2JsonRedisSerializer<T> {
        val type = javaType ?: objectMapper.typeFactory.constructType(T::class.java)
        return Jackson2JsonRedisSerializer(objectMapper, type)
    }

    private fun createCacheConfigurationsForKeys(
        keys: List<String>,
        ttl: Duration,
        serializer: Jackson2JsonRedisSerializer<*>
    ): Map<String, RedisCacheConfiguration> {
        return keys.associateWith { createCacheConfiguration(ttl, serializer) }
    }

    private fun createCacheConfiguration(
        ttl: Duration,
        valueSerializer: Jackson2JsonRedisSerializer<*>
    ): RedisCacheConfiguration {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
    }
}
