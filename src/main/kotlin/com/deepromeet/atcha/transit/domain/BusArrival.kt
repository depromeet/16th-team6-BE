package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime

data class BusArrival(
    val routeId: RouteId,
    val stationId: StationId,
    val lastTime: LocalDateTime,
    val realTimeInfo: List<RealTimeBusArrival>
)

data class RealTimeBusArrival(
    val busStatus: BusStatus,
    val remainingTime: Int,
    val remainingStations: Int,
    val currentStation: String,
    val isLast: Boolean
)
