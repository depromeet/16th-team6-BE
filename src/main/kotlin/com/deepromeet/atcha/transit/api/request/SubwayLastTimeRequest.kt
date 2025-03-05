package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.transit.domain.SubwayDirection
import com.deepromeet.atcha.transit.domain.SubwayStationMeta

data class SubwayLastTimeRequest(
    val startStationName: String,
    val endStationName: String,
    val routeName: String,
    val direction: SubwayDirection
) {
    fun toStartMeta(): SubwayStationMeta {
        return SubwayStationMeta(startStationName, routeName)
    }

    fun toEndMeta(): SubwayStationMeta {
        return SubwayStationMeta(endStationName, routeName)
    }
}
