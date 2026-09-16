package com.deepromeet.atcha.shared.infrastructure.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

private val log = KotlinLogging.logger {}

/**
 * Redis 기반 [CacheStore] 어댑터.
 *
 * 조회 실패는 삼켜서 캐시 미스(null)로 처리하고, [metric] 이 주어지면 히트/미스를 기록한다.
 * 저장 실패 역시 삼켜서 캐시가 비즈니스 흐름을 막지 않도록 한다.
 */
class RedisCacheStore<V : Any>(
    private val template: RedisTemplate<String, V>,
    private val recorder: RedisCacheHitRecorder,
    private val metric: String? = null
) : CacheStore<V> {
    override fun get(key: String): V? =
        try {
            val value = template.opsForValue().get(key)
            metric?.let { recorder.record(it, value != null) }
            value
        } catch (e: Exception) {
            log.warn { "캐시 조회 중 오류 발생 (key=$key): ${e.message}" }
            metric?.let { recorder.record(it, false) }
            null
        }

    override fun put(
        key: String,
        value: V
    ) {
        try {
            template.opsForValue().set(key, value)
        } catch (e: Exception) {
            log.warn { "캐시 저장 중 오류 발생 (key=$key): ${e.message}" }
        }
    }

    override fun put(
        key: String,
        value: V,
        ttl: Duration
    ) {
        try {
            template.opsForValue().set(key, value, ttl)
        } catch (e: Exception) {
            log.warn { "캐시 저장 중 오류 발생 (key=$key): ${e.message}" }
        }
    }
}
