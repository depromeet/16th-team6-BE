package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusStationMeta

data class BusArrivalRequest(
    val routeName: String,
    val stationName: String,
    val lat: Double,
    val lon: Double
) {
    fun toBusStationMeta() =
        BusStationMeta(
            stationName,
            Coordinate(lat, lon)
        )
}
