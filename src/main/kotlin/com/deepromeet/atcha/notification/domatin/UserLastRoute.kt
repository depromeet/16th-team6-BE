package com.deepromeet.atcha.notification.domatin

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserLastRoute(
    val departureTime: String,
    val updatedAt: String,
    val token: String,
    val lastRouteId: String,
    val userId: Long
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    constructor(
        token: String,
        departureTime: LocalDateTime,
        routeId: String,
        userId: Long
    ) : this(
        token = token,
        updatedAt = LocalDateTime.now().format(dateTimeFormatter),
        departureTime = departureTime.format(dateTimeFormatter),
        lastRouteId = routeId,
        userId = userId
    )

    fun updateDepartureTime(newDepartureTime: LocalDateTime) =
        this.copy(
            departureTime = newDepartureTime.format(dateTimeFormatter),
            updatedAt = LocalDateTime.now().format(dateTimeFormatter)
        )
}
