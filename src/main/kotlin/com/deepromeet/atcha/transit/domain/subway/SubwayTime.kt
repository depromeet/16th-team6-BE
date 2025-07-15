package com.deepromeet.atcha.transit.domain.subway

import java.time.LocalDateTime

data class SubwayTime(
    val isExpress: Boolean,
    val finalStation: SubwayStation,
    val departureTime: LocalDateTime,
    val subwayDirection: SubwayDirection
)
