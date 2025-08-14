package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.user.domain.UserId
import java.time.Duration

interface UserRouteRepository {
    fun save(
        userRoute: UserRoute,
        duration: Duration
    ): UserRoute

    fun findById(userId: UserId): UserRoute?

    fun findAll(): List<UserRoute>

    fun delete(userId: UserId)
}
