package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.Station
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.PassStopList

data class BusArrivalRequest(
    val routeName: String,
    val stationName: String,
    val passStations: List<Station>,
    val lat: Double,
    val lon: Double
) {
    fun toBusStationMeta() =
        BusStationMeta(
            stationName,
            Coordinate(lat, lon)
        )

    fun toRouteName(): String {
        return routeName.split(":")[1]
    }

    fun toPassStopList(): PassStopList {
        return PassStopList(
            passStations
        )
    }
}
