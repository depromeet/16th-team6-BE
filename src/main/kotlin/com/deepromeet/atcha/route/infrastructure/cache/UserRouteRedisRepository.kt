package com.deepromeet.atcha.route.infrastructure.cache

import com.deepromeet.atcha.route.application.UserRouteRepository
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.user.domain.UserId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class UserRouteRedisRepository(
    private val userRouteRedisTemplate: RedisTemplate<String, UserRoute>
) : UserRouteRepository {
    private val duration = Duration.ofDays(1)
    private val valueOps = userRouteRedisTemplate.opsForValue()
    private val scanOptions =
        ScanOptions
            .scanOptions()
            .match("user-routes:[0-9]*")
            .count(1000)
            .build()

    override fun save(userRoute: UserRoute): UserRoute {
        valueOps.set(getKey(userRoute.userId), userRoute, duration)
        return userRoute
    }

    override fun findById(userId: UserId): UserRoute? = valueOps.get(getKey(userId))

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

    override fun delete(userId: UserId) {
        userRouteRedisTemplate.delete(getKey(userId))
    }

    private fun getKey(userId: UserId) = "user-routes:${userId.value}"
}
