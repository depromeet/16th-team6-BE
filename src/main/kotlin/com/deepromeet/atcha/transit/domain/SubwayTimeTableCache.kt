package com.deepromeet.atcha.transit.domain

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
