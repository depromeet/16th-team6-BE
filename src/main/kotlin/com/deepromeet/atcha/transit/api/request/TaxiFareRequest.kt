package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class TaxiFareRequest(
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double
) {
    fun toOrigin(): Coordinate = Coordinate(startLat, startLon)

    fun toDestination(): Coordinate = Coordinate(endLat, endLon)
}
