package com.deepromeet.atcha.location.api.response

import com.deepromeet.atcha.location.domain.Location

data class LocationResponse(
    val name: String,
    val lat: Double,
    val lon: Double
) {
    companion object {
        fun from(domain: Location): LocationResponse {
            return LocationResponse(
                name = domain.name,
                lat = domain.coordinate.lat,
                lon = domain.coordinate.lon
            )
        }
    }
}
