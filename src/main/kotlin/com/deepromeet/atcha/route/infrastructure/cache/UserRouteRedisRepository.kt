package com.deepromeet.atcha.route.infrastructure.cache

import com.deepromeet.atcha.route.application.UserRouteRepository
import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.user.domain.UserId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class UserRouteRedisRepository(
    private val userRouteRedisTemplate: RedisTemplate<String, UserRoute>
) : UserRouteRepository {
    private val valueOps = userRouteRedisTemplate.opsForValue()
    private val scanOptions =
        ScanOptions
            .scanOptions()
            .match("user-routes:[0-9]*")
            .count(1000)
            .build()

    override fun save(
        userRoute: UserRoute,
        duration: Duration
    ): UserRoute {
        try {
            valueOps.set(getKey(userRoute.userId), userRoute, duration)
        } catch (e: Exception) {
            logger.warn { "사용자 경로 저장 중 오류 발생: ${e.message}" }
        }
        return userRoute
    }

    override fun findById(userId: UserId): UserRoute? {
        return try {
            valueOps.get(getKey(userId))
        } catch (e: Exception) {
            logger.warn { "사용자 경로 조회 중 오류 발생: ${e.message}" }
            null
        }
    }

    override fun findAll(): List<UserRoute> {
        return try {
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
            result
        } catch (e: Exception) {
            logger.warn { "전체 사용자 경로 조회 중 오류 발생: ${e.message}" }
            emptyList()
        }
    }

    override fun delete(userId: UserId) {
        try {
            userRouteRedisTemplate.delete(getKey(userId))
        } catch (e: Exception) {
            logger.warn { "사용자 경로 삭제 중 오류 발생: ${e.message}" }
        }
    }

    private fun getKey(userId: UserId) = "user-routes:${userId.value}"
}
