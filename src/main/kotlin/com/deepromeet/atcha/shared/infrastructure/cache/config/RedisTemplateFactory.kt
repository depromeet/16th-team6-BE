package com.deepromeet.atcha.shared.infrastructure.cache.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component

/**
 * 모든 캐시 RedisTemplate 생성을 한 곳으로 모은 깊은 모듈(deep module).
 *
 * 도메인별 *RedisConfig 들이 각자 손으로 만들던 ObjectMapper(KotlinModule + JavaTimeModule +
 * FAIL_ON_UNKNOWN_PROPERTIES=false + WRITE_DATES_AS_TIMESTAMPS=false)와 StringKey/JsonValue
 * 직렬화 배선을 이 인터페이스 뒤로 흡수한다. 호출부는 값 타입(과 필요 시 ObjectMapper 커스터마이징)만 넘긴다.
 */
@Component
class RedisTemplateFactory(
    private val connectionFactory: RedisConnectionFactory
) {
    private fun baseMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    /** 단일 타입 값 캐시. 필요하면 [customize]로 MixIn 등 ObjectMapper 를 보강한다. */
    fun <T> create(
        type: Class<T>,
        customize: (ObjectMapper) -> Unit = {}
    ): RedisTemplate<String, T> {
        val mapper = baseMapper().apply(customize)
        return build(Jackson2JsonRedisSerializer(mapper, type))
    }

    /** List<T> 등 제네릭 타입 값 캐시. */
    fun <T> create(
        type: TypeReference<T>,
        customize: (ObjectMapper) -> Unit = {}
    ): RedisTemplate<String, T> {
        val mapper = baseMapper().apply(customize)
        val javaType = mapper.typeFactory.constructType(type)
        return build(Jackson2JsonRedisSerializer<T>(mapper, javaType))
    }

    /**
     * 직렬화된 값에 `@class` 타입 정보를 함께 저장하는 제네릭 직렬화 버전.
     * 기존 휴일 캐시처럼 타입 메타를 포함한 포맷을 유지해야 하는 곳에서 사용한다.
     */
    fun <T> createGeneric(): RedisTemplate<String, T> = build(GenericJackson2JsonRedisSerializer(baseMapper()))

    private fun <T> build(serializer: RedisSerializer<*>): RedisTemplate<String, T> {
        return RedisTemplate<String, T>().apply {
            connectionFactory = this@RedisTemplateFactory.connectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = serializer
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = serializer
        }
    }
}
