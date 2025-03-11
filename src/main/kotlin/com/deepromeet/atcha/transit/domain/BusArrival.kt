package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime

data class BusArrival(
    val routeId: RouteId,
    val routeName: String,
    val busStationId: BusStationId,
    val stationName: String,
    val lastTime: LocalDateTime,
    val term: Int,
    val realTimeInfo: List<RealTimeBusArrival>
) {
    fun getNearestTime(
        time: LocalDateTime,
        timeDirection: TimeDirection
    ): LocalDateTime {
        var current = lastTime

        return when (timeDirection) {
            TimeDirection.BEFORE -> {
                while (current.isAfter(time)) {
                    current = current.minusMinutes(term.toLong())
                }
                current
            }
            TimeDirection.AFTER -> {
                while (current.isBefore(time)) {
                    current = current.plusMinutes(term.toLong())
                }
                current
            }
        }
    }
}

data class RealTimeBusArrival(
    val busStatus: BusStatus,
    val remainingTime: Int,
    val remainingStations: Int,
    val isLast: Boolean?
) {
    val expectedArrivalTime: LocalDateTime?
        get() =
            when (busStatus) {
                BusStatus.OPERATING, BusStatus.SOON ->
                    LocalDateTime.now()
                        .plusSeconds(remainingTime.toLong())
                else -> null
            }
}
