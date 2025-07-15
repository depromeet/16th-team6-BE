package com.deepromeet.atcha.notification.domain

interface UserLastRouteRepository {
    fun save(userLastRoute: UserLastRoute): UserLastRoute

    fun findById(
        userId: Long,
        routeId: String
    ): UserLastRoute?

    fun findAll(): List<UserLastRoute>

    fun update(userLastRoute: UserLastRoute)

    fun delete(
        userId: Long,
        routeId: String
    )
}
