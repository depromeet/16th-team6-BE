package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.TimeDirection
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.time.LocalDateTime

data class SubwayTimeTable(
    val startStation: SubwayStation,
    val dailyType: DailyType,
    val subwayDirection: SubwayDirection,
    val schedules: List<SubwaySchedule>
) {
    fun getLastTime(
        destinationStation: SubwayStation,
        routes: List<Route>,
        isExpress: Boolean
    ): SubwaySchedule =
        schedules
            .filter { it.isExpress == isExpress }
            .filter { isReachable(startStation, destinationStation, it.finalStation, routes, subwayDirection) }
            .maxByOrNull { it.departureTime.toLocalDateTime() }
            ?: throw TransitException.of(
                TransitError.NOT_FOUND_SUBWAY_LAST_TIME,
                "${startStation.routeName} 지하철 '${startStation.name}'역에서" +
                    " '${destinationStation.name}'역으로 가는 " +
                    "'${subwayDirection.getName(SubwayLine.fromRouteName(startStation.routeName).isCircular)}' " +
                    "방향 막차 시간을 찾을 수 없습니다."
            )

    fun findNearestTime(
        time: LocalDateTime,
        direction: TimeDirection
    ): SubwaySchedule? =
        when (direction) {
            TimeDirection.AFTER -> {
                schedules
                    .filter { it.departureTime.toLocalDateTime().isAfter(time) }
                    .minByOrNull { it.departureTime.toLocalDateTime() }
            }

            TimeDirection.BEFORE -> {
                schedules
                    .filter { it.departureTime.toLocalDateTime().isBefore(time) }
                    .maxByOrNull { it.departureTime.toLocalDateTime() }
            }
        }

    private fun isReachable(
        startStation: SubwayStation,
        endStation: SubwayStation,
        finalStation: SubwayStation,
        routes: List<Route>,
        direction: SubwayDirection
    ): Boolean {
        return routes.any { route ->
            route.isReachable(startStation.name, endStation.name, finalStation.name, direction)
        }
    }
}
