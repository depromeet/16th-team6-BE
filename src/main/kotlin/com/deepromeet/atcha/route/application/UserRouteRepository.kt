package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.UserRoute

interface UserRouteRepository {
    fun save(userRoute: UserRoute): UserRoute

    fun findById(
        userId: Long,
        routeId: String
    ): UserRoute?

    fun findAll(): List<UserRoute>

    fun update(userRoute: UserRoute)

    fun delete(
        userId: Long,
        routeId: String
    )
}
