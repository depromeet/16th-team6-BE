package com.deepromeet.atcha.route.infrastructure.redis

import com.deepromeet.atcha.route.application.UserRouteRepository
import com.deepromeet.atcha.route.domain.UserRoute
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserRouteRedisRepository(
    private val userRouteRedisTemplate: RedisTemplate<String, UserRoute>
) : UserRouteRepository {
    private val duration = Duration.ofHours(2)
    private val valueOps = userRouteRedisTemplate.opsForValue()
    private val scanOptions =
        ScanOptions
            .scanOptions()
            .match("user-routes:[0-9]*")
            .count(1000)
            .build()

    override fun save(userRoute: UserRoute): UserRoute {
        valueOps.set(getKey(userRoute), userRoute, duration)
        return userRoute
    }

    override fun findById(
        userId: Long,
        routeId: String
    ): UserRoute? = valueOps.get(getKey(userId, routeId))

    override fun findAll(): List<UserRoute> {
        val result = mutableListOf<UserRoute>()
        userRouteRedisTemplate.scan(scanOptions).use { cursor ->
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

    override fun update(userRoute: UserRoute) {
        save(userRoute)
    }

    override fun delete(
        userId: Long,
        routeId: String
    ) {
        userRouteRedisTemplate.delete(getKey(userId, routeId))
    }

    private fun getKey(userRoute: UserRoute) = "user-routes:${userRoute.userId}:${userRoute.lastRouteId}"

    private fun getKey(
        userId: Long,
        lastRouteId: String
    ) = "user-routes:$userId:$lastRouteId"
}
