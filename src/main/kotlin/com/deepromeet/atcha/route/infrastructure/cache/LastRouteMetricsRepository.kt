package com.deepromeet.atcha.route.infrastructure.cache

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class LastRouteMetricsRepository(
    private val stringRedisTemplate: StringRedisTemplate
) {
    private fun today(): String = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) // 20250702

    fun incrTotal(count: Long = 1) {
        val key = "last_route:${today()}:total"
        stringRedisTemplate.opsForValue().increment(key, count)
        stringRedisTemplate.expireIfNotSet(key)
        stringRedisTemplate.opsForValue().increment("last_route:total", count)
    }

    fun incrSuccess(count: Long = 1) {
        val key = "last_route:${today()}:success"
        stringRedisTemplate.opsForValue().increment(key, count)
        stringRedisTemplate.expireIfNotSet(key)
        stringRedisTemplate.opsForValue().increment("last_route:success", count)
    }

    fun getTodayRate(): Double {
        val total =
            stringRedisTemplate.opsForValue()
                .get("last_route:${today()}:total")?.toDouble() ?: 0.0
        val success =
            stringRedisTemplate.opsForValue()
                .get("last_route:${today()}:success")?.toDouble() ?: 0.0
        return if (total == 0.0) 0.0 else (success / total) * 100
    }

    /** 처음 SET 시에만 TTL 적용 */
    private fun StringRedisTemplate.expireIfNotSet(key: String) {
        if (this.getExpire(key) == -1L) {
            this.expire(key, Duration.ofDays(14))
        }
    }
}
