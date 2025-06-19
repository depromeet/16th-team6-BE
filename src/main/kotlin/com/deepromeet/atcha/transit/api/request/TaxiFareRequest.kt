package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.exception.LocationException

data class TaxiFareRequest(
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double
) {
    init {
        require(startLat in -90.0..90.0) {
            throw LocationException.InvalidLatitude
        }
        require(startLon in -180.0..180.0) {
            throw LocationException.InvalidLongitude
        }
        require(endLat in -90.0..90.0) {
            throw LocationException.InvalidLatitude
        }
        require(endLon in -180.0..180.0) {
            throw LocationException.InvalidLongitude
        }
    }

    fun toStart(): Coordinate = Coordinate(startLat, startLon)

    fun toEnd(): Coordinate = Coordinate(endLat, endLon)
}
