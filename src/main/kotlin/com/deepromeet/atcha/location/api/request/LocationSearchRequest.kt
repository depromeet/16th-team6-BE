package com.deepromeet.atcha.location.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class LocationSearchRequest(
    val keyword: String,
    val lat: Double,
    val lon: Double
) {
    fun toCoordinate() = Coordinate(lat, lon)
}
