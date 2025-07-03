package com.deepromeet.atcha.transit.domain

interface SubwayTimetableClient {
    suspend fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable
}
