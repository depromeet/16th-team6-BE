package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class TaxiFareRequest(
    val originLat: Double,
    val originLon: Double,
    val destinationLat: Double,
    val destinationLon: Double
) {
    fun toOriginCoordinate(): Coordinate = Coordinate(originLat, originLon)

    fun toDestinationCoordinate(): Coordinate = Coordinate(destinationLat, destinationLon)
}
