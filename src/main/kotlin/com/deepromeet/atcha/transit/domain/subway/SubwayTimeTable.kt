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
    fun getLastTime(): SubwaySchedule = schedules.maxBy { it.departureTime.toLocalDateTime() }

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

    fun filterReachable(
        endStation: SubwayStation,
        routes: List<Route>,
        isExpress: Boolean
    ): SubwayTimeTable {
        val reachableSchedules =
            schedules.filter { isReachable(endStation, it.finalStation, routes) }
                .filter { it.isExpress == isExpress }
                .ifEmpty {
                    throw TransitException(
                        TransitError.NOT_FOUND_SUBWAY_LAST_TIME,
                        "'${endStation.routeName}'노선의 '${endStation.name}'역으로 가는 유효한 시간표가 없습니다."
                    )
                }

        return copy(schedules = reachableSchedules)
    }

    fun isReachable(
        endStation: SubwayStation,
        finalStation: SubwayStation,
        routes: List<Route>
    ): Boolean {
        return routes.any { route ->
            route.isReachable(startStation.name, endStation.name, finalStation.name, subwayDirection)
        }
    }
}
