package com.deepromeet.atcha.transit.domain

data class RoutePassStops(
    val stops: List<RoutePassStop> = emptyList()
) {
    companion object {
        fun of(stops: List<RoutePassStop>): RoutePassStops = RoutePassStops(stops)
    }

    fun getNextStationName(): String {
        return stops[1].stationName
    }
}
