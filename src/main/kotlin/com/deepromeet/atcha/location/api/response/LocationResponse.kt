package com.deepromeet.atcha.location.api.response

import com.deepromeet.atcha.location.domain.Location

data class LocationResponse(
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double
) {
    constructor(domain: Location) : this(
        name = domain.name,
        address = domain.address,
        lat = domain.coordinate.lat,
        lon = domain.coordinate.lon
    )
}
