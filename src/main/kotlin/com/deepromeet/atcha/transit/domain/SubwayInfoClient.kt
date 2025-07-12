package com.deepromeet.atcha.transit.domain

interface SubwayInfoClient {
    suspend fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable?
}
