package com.deepromeet.atcha.route.domain

import java.time.LocalDateTime

fun List<LastRoute>.sort(sortType: LastRouteSortType = LastRouteSortType.DEPARTURE_TIME_DESC): List<LastRoute> {
    val now = LocalDateTime.now()
    val upcomingRoutes =
        this.filter {
            it.departureDateTime.isAfter(now)
        }

    return when (sortType) {
        LastRouteSortType.MINIMUM_TRANSFERS ->
            upcomingRoutes.sortedWith(
                compareBy({ it.transferCount }, { it.totalTime })
            )
        LastRouteSortType.DEPARTURE_TIME_DESC -> upcomingRoutes.sortedByDescending { it.departureDateTime }
    }
}
