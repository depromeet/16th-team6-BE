package com.deepromeet.atcha.transit.application.subway

import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwayStation

interface SubwayStationCache {
    fun get(
        subwayLine: SubwayLine,
        stationName: String
    ): SubwayStation?

    fun cache(
        subwayLine: SubwayLine,
        stationName: String,
        subwayStation: SubwayStation
    )
}
