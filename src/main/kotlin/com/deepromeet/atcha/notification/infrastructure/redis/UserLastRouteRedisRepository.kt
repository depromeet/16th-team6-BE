package com.deepromeet.atcha.notification.infrastructure.redis

import com.deepromeet.atcha.notification.domatin.UserLastRoute
import com.deepromeet.atcha.notification.domatin.UserLastRouteRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserLastRouteRedisRepository(
    private val userLastRouteRedisTemplate: RedisTemplate<String, UserLastRoute>
) : UserLastRouteRepository {
    private val duration = Duration.ofHours(2)
    private val valueOps = userLastRouteRedisTemplate.opsForValue()
    private val scanOptions =
        ScanOptions
            .scanOptions()
            .match("notification:[0-9]*")
            .count(1000)
            .build()

    override fun save(userLastRoute: UserLastRoute): UserLastRoute {
        valueOps.set(getKey(userLastRoute), userLastRoute, duration)
        return userLastRoute
    }

    override fun findById(
        userId: Long,
        routeId: String
    ): UserLastRoute? = valueOps.get(getKey(userId, routeId))

    override fun findAll(): List<UserLastRoute> {
        val result = mutableListOf<UserLastRoute>()
        userLastRouteRedisTemplate.scan(scanOptions).use { cursor ->
            while (cursor.hasNext()) {
                val key = cursor.next()
                val value = valueOps.get(key)
                if (value != null) {
                    result.add(value)
                }
            }
        }
        return result
    }

    override fun update(userLastRoute: UserLastRoute) {
        save(userLastRoute)
    }

    override fun delete(
        userId: Long,
        routeId: String
    ) {
        userLastRouteRedisTemplate.delete(getKey(userId, routeId))
    }

    private fun getKey(userLastRoute: UserLastRoute) =
        "notification:${userLastRoute.userId}:${userLastRoute.lastRouteId}"

    private fun getKey(
        userId: Long,
        lastRouteId: String
    ) = "notification:$userId:$lastRouteId"
}
