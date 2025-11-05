package com.deepromeet.atcha.route.api.response

import com.deepromeet.atcha.route.domain.UserRoute
import java.time.format.DateTimeFormatter

data class UserRouteResponse(
    val departureTime: String,
    val updatedAt: String,
    val lastRouteId: String
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    constructor(
        userRoute: UserRoute
    ) : this(
        userRoute.updatedDepartureTime.format(dateTimeFormatter),
        userRoute.updatedAt.format(dateTimeFormatter),
        userRoute.lastRouteId
    )
}
