package com.deepromeet.atcha.route.api.response

import com.deepromeet.atcha.route.domain.UserRoute

data class UserRouteResponse(
    val departureTime: String,
    val updatedAt: String,
    val lastRouteId: String
) {
    constructor(
        userRoute: UserRoute
    ) : this(
        userRoute.departureTime,
        userRoute.updatedAt,
        userRoute.lastRouteId
    )
}
