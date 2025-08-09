package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.user.domain.UserId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserRoute(
    val baseDepartureTime: String,
    val updatedDepartureTime: String,
    val updatedAt: String,
    val token: String,
    val lastRouteId: String,
    val userId: UserId
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    constructor(
        token: String,
        departureTime: String,
        routeId: String,
        userId: UserId
    ) : this(
        token = token,
        updatedAt = LocalDateTime.now().format(dateTimeFormatter),
        baseDepartureTime = departureTime,
        updatedDepartureTime = departureTime,
        lastRouteId = routeId,
        userId = userId
    )

    fun updateDepartureTime(newDepartureTime: LocalDateTime) =
        this.copy(
            updatedDepartureTime = newDepartureTime.format(dateTimeFormatter),
            updatedAt = LocalDateTime.now().format(dateTimeFormatter)
        )

    fun parseBaseDepartureTime(): LocalDateTime = LocalDateTime.parse(baseDepartureTime, dateTimeFormatter)

    fun parseUpdatedDepartureTime(): LocalDateTime = LocalDateTime.parse(updatedDepartureTime, dateTimeFormatter)

    fun parseUpdatedAt(): LocalDateTime = LocalDateTime.parse(updatedAt, dateTimeFormatter)
}
