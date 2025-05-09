package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime

data class SubwayTime(
    val finalStation: SubwayStation,
    val arrivalTime: LocalDateTime?,
    val departureTime: LocalDateTime?,
    val subwayDirection: SubwayDirection
)
