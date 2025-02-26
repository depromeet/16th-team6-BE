package com.deepromeet.atcha.location.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location
import com.deepromeet.atcha.location.domain.POI

data class POIHistoryRequest(
    val name: String,
    val lat: Double,
    val lon: Double,
    val businessCategory: String,
    val address: String
) {
    fun toPOI(): POI {
        return POI(
            location =
                Location(
                    name = name,
                    coordinate =
                        Coordinate(
                            lat = lat,
                            lon = lon
                        )
                ),
            businessCategory = businessCategory,
            address = address
        )
    }
}
