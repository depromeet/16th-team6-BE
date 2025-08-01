package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.UserRoute
import com.deepromeet.atcha.user.domain.UserId

interface UserRouteRepository {
    fun save(userRoute: UserRoute): UserRoute

    fun findById(userId: UserId): UserRoute?

    fun findAll(): List<UserRoute>

    fun update(userRoute: UserRoute)

    fun delete(userId: UserId)
}
