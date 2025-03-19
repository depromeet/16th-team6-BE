package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime

data class BusRouteInfo(
    val startStationName: String,
    val endStationName: String,
    val serviceHours: List<BusServiceHours>
)

data class BusServiceHours(
    val dailyType: DailyType,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val term: Int
)
