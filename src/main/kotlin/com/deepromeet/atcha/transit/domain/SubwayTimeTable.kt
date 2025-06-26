package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.time.LocalDateTime

data class SubwayTimeTable(
    val startStation: SubwayStation,
    val dailyType: DailyType,
    val subwayDirection: SubwayDirection,
    val schedule: List<SubwayTime>
) {
    fun getLastTime(
        destinationStation: SubwayStation,
        routes: List<Route>
    ): SubwayTime =
        schedule
            .filter { isReachable(startStation, destinationStation, it.finalStation, routes) }
            .maxByOrNull { it.departureTime }
            ?: throw TransitException.of(
                TransitError.NOT_FOUND_SUBWAY_LAST_TIME,
                "지하철 '${startStation.name}'역에서 '${destinationStation.name}'역으로 가는 막차 시간을 찾을 수 없습니다."
            )

    fun findNearestTime(
        time: LocalDateTime,
        direction: TimeDirection
    ): SubwayTime? =
        when (direction) {
            TimeDirection.AFTER -> {
                schedule
                    .filter { it.departureTime.isAfter(time) }
                    .minByOrNull { it.departureTime }
            }

            TimeDirection.BEFORE -> {
                schedule
                    .filter { it.departureTime.isBefore(time) }
                    .maxByOrNull { it.departureTime }
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
