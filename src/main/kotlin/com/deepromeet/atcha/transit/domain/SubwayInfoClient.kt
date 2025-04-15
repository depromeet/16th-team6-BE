package com.deepromeet.atcha.transit.domain

interface SubwayInfoClient {
    fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable?
}
