package com.deepromeet.atcha.shared.infrastructure.cache

import com.deepromeet.atcha.shared.domain.event.domain.CacheMetrics
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisCacheHitRecorder(
    private val stringRedisTemplate: StringRedisTemplate
) {
    private val namespace = "cache:metrics"

    private fun metricsKey(key: String): String {
        return "$namespace:$key"
    }

    fun record(
        key: String,
        hit: Boolean
    ) {
        val k = metricsKey(key)
        val ops = stringRedisTemplate.opsForHash<String, Any>()
        ops.increment(k, "total", 1)
        ops.increment(k, if (hit) "hit" else "miss", 1)
    }

    fun snapshot(key: String): CacheMetrics {
        val k = metricsKey(key)
        val ops = stringRedisTemplate.opsForHash<String, String>()
        val values = ops.multiGet(k, listOf("hit", "miss"))
        val hit = values.getOrNull(0)?.toLongOrNull() ?: 0L
        val miss = values.getOrNull(1)?.toLongOrNull() ?: 0L
        return CacheMetrics(hit = hit, miss = miss)
    }

    fun reset(key: String) {
        stringRedisTemplate.delete(metricsKey(key))
    }
}
