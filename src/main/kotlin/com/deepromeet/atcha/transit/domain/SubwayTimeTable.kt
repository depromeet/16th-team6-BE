package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime
import java.time.LocalTime

data class SubwayTimeTable(
    val startStation: SubwayStation,
    val dailyType: DailyType,
    val subwayDirection: SubwayDirection,
    val schedule: List<SubwayTime>
) {
    fun getLastTime(
        endStation: SubwayStation,
        routes: List<Route>
    ): SubwayTime? =
        schedule
            .filter {
                it.departureTime
                    ?.isAfter(LocalDateTime.of(it.departureTime.toLocalDate(), LocalTime.of(21, 0)))
                    ?: false
            }
            .filter { isReachable(startStation, endStation, it.finalStation, routes) }
            .maxWithOrNull(compareBy(nullsLast()) { it.departureTime })

    private fun isReachable(
        startStation: SubwayStation,
        endStation: SubwayStation,
        finalStation: SubwayStation,
        routes: List<Route>
    ): Boolean {
        return routes.any { route ->
            route.isReachable(startStation.name, endStation.name, finalStation.name)
        }
    }
}
