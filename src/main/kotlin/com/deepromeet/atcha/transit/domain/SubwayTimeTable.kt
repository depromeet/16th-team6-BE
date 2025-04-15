package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitException
import java.time.LocalDateTime
import java.time.LocalTime

data class SubwayTimeTable(
    val startStation: SubwayStation,
    val dailyType: DailyType,
    val subwayDirection: SubwayDirection,
    val schedule: List<SubwayTime>
) {
    fun getLastTime(
        destinationStation: SubwayStation,
        routes: List<Route>
    ): SubwayTime? =
        schedule
            .filter {
                it.departureTime
                    ?.isAfter(LocalDateTime.of(it.departureTime.toLocalDate(), LocalTime.of(21, 0)))
                    ?: false
            }
            .filter { isReachable(startStation, destinationStation, it.finalStation, routes) }
            .maxWithOrNull(compareBy(nullsLast()) { it.departureTime })

    fun findNearestTime(
        time: LocalDateTime,
        direction: TimeDirection
    ): SubwayTime? =
        when (direction) {
            TimeDirection.AFTER -> {
                schedule
                    .filter { it.departureTime?.isAfter(time) ?: false }
                    .minByOrNull { it.departureTime ?: throw TransitException.NotFoundTime }
            }

            TimeDirection.BEFORE -> {
                schedule
                    .filter { it.departureTime?.isBefore(time) ?: false }
                    .maxByOrNull { it.departureTime ?: throw TransitException.NotFoundTime }
            }
        }

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
