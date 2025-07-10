package com.deepromeet.atcha.notification.domatin

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
