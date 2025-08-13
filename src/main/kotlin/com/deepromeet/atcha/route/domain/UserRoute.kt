package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.user.domain.UserId
import java.time.LocalDateTime

data class UserRoute(
    val baseDepartureTime: LocalDateTime,
    val updatedDepartureTime: LocalDateTime,
    val updatedAt: LocalDateTime,
    val token: String,
    val lastRouteId: String,
    val userId: UserId
) {
    constructor(
        token: String,
        departureTime: LocalDateTime,
        routeId: String,
        userId: UserId
    ) : this(
        token = token,
        updatedAt = LocalDateTime.now(),
        baseDepartureTime = departureTime,
        updatedDepartureTime = departureTime,
        lastRouteId = routeId,
        userId = userId
    )

    fun updateDepartureTime(newDepartureTime: LocalDateTime) =
        this.copy(
            updatedDepartureTime = newDepartureTime,
            updatedAt = LocalDateTime.now()
        )
}
