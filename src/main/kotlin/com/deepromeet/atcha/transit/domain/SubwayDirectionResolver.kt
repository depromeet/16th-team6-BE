package com.deepromeet.atcha.transit.domain

object SubwayDirectionResolver {
    fun resolve(
        startStation: SubwayStation,
        endStation: SubwayStation
    ): SubwayDirection {
        return if (startStation.ord < endStation.ord) {
            SubwayDirection.UP
        } else {
            SubwayDirection.DOWN
        }
    }
}
