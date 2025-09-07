package com.deepromeet.atcha.transit.domain.subway

data class SubwaySchedule(
    val isExpress: Boolean,
    val finalStation: SubwayStation,
    val departureTime: SubwayTime,
    val subwayDirection: SubwayDirection
)
