package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import com.deepromeet.atcha.transit.domain.LastRouteCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LastRouteRedisCache(
    private val lastRouteRedisTemplate: RedisTemplate<String, LastRoutesResponse>
) : LastRouteCache {
    override fun processRoutes(action: (LastRoutesResponse) -> Unit) {
        val valueOps = lastRouteRedisTemplate.opsForValue()

        lastRouteRedisTemplate.execute { connection ->
            val scanOptions =
                ScanOptions.scanOptions()
                    .match("routes:last:*")
                    .count(1000)
                    .build()

            val cursor = connection.scan(scanOptions)
            cursor.use {
                while (it.hasNext()) {
                    val keyBytes = it.next()
                    val keyString = String(keyBytes, Charsets.UTF_8)

                    valueOps.get(keyString)?.let { routeValue ->
                        action(routeValue)
                    }
                }
            }
        }
    }

    override fun get(routeId: String): LastRoutesResponse? {
        return lastRouteRedisTemplate.opsForValue().get(getKey(routeId))
    }

    override fun cache(route: LastRoutesResponse) {
        lastRouteRedisTemplate.opsForValue().set(getKey(route.routeId), route, Duration.ofHours(12))
    }

    fun getKey(routeId: String): String {
        return "routes:last:$routeId"
    }
}
