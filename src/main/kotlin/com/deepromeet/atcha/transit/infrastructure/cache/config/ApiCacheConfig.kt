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
        val busRouteStationListType: JavaType = objectMapper.typeFactory.constructType(BusRouteStationList::class.java)
        val busRouteListType: JavaType =
            objectMapper.typeFactory.constructType(
                object : TypeReference<List<BusRoute>>() {}
            )
        val busRouteInfoType: JavaType = objectMapper.typeFactory.constructType(BusRouteInfo::class.java)

        val busRouteStationListSerializer =
            Jackson2JsonRedisSerializer<BusRouteStationList>(objectMapper, busRouteStationListType)
        val busRouteListSerializer = Jackson2JsonRedisSerializer<List<BusRoute>>(objectMapper, busRouteListType)
        val busRouteInfoSerializer = Jackson2JsonRedisSerializer<BusRouteInfo>(objectMapper, busRouteInfoType)

        val cacheConfigurations =
            mapOf(
                "api:seoul:busRouteStationList" to
                    createCacheConfiguration(
                        Duration.ofDays(7),
                        busRouteStationListSerializer
                    ),
                "api:incheon:busRouteStationList" to
                    createCacheConfiguration(
                        Duration.ofDays(7),
                        busRouteStationListSerializer
                    ),
                "api:gyeonggi:busRouteStationList" to
                    createCacheConfiguration(
                        Duration.ofDays(7),
                        busRouteStationListSerializer
                    ),
                "api:seoul:busRouteList" to
                    createCacheConfiguration(
                        Duration.ofDays(7),
                        busRouteListSerializer
                    ),
                "api:incheon:busRouteList" to
                    createCacheConfiguration(
                        Duration.ofDays(7),
                        busRouteListSerializer
                    ),
                "api:gyeonggi:busRouteList" to
                    createCacheConfiguration(
                        Duration.ofDays(7),
                        busRouteListSerializer
                    ),
                "busRouteInfo" to
                    createCacheConfiguration(
                        Duration.ofHours(1),
                        busRouteInfoSerializer
                    )
            )

        return RedisCacheManager.builder(redisConnectionFactory)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
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
