package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.route.exception.RouteError
import com.deepromeet.atcha.route.exception.RouteException
import java.time.LocalDateTime

fun List<LastRoute>.sort(sortType: LastRouteSortType = LastRouteSortType.DEPARTURE_TIME_DESC): List<LastRoute> {
    val now = LocalDateTime.now()
    val upcomingRoutes =
        this.filter {
            it.departureDateTime.isAfter(now)
        }

    if (upcomingRoutes.isEmpty()) {
        throw RouteException.of(RouteError.NO_AVAILABLE_LAST_ROUTES)
    }

    return when (sortType) {
        LastRouteSortType.MINIMUM_TRANSFERS ->
            upcomingRoutes.sortedWith(
                compareBy({ it.transferCount }, { it.totalTime })
            )
        LastRouteSortType.DEPARTURE_TIME_DESC -> upcomingRoutes.sortedByDescending { it.departureDateTime }
    }
}
