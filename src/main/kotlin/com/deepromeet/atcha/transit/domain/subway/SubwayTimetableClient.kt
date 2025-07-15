package com.deepromeet.atcha.transit.domain.subway

import com.deepromeet.atcha.transit.domain.DailyType

interface SubwayTimetableClient {
    suspend fun getTimeTable(
        startStation: SubwayStation,
        dailyType: DailyType,
        direction: SubwayDirection
    ): SubwayTimeTable
}
