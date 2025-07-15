package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.domain.DailyType

interface SubwayTimeTableCache {
    fun get(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable?

    fun cache(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection,
        timeTable: SubwayTimeTable
    )
}
