package com.deepromeet.atcha.location.api.response

import com.deepromeet.atcha.location.domain.Location

data class LocationResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val businessCategory: String,
    val address: String,
    val radius: String
) {
    companion object {
        fun from(domain: Location): LocationResponse {
            return LocationResponse(
                name = domain.name,
                lat = domain.coordinate.lat,
                lon = domain.coordinate.lon,
                businessCategory = domain.businessCategory,
                address = domain.address,
                radius = "${domain.radius}km"
            )
        }
    }
}
