package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.location.domain.Coordinate

data class RouteLocation(
    val name: String,
    val coordinate: Coordinate
) {
    val latitude: Double get() = coordinate.lat
    val longitude: Double get() = coordinate.lon

    companion object {
        fun of(
            name: String,
            latitude: Double,
            longitude: Double
        ): RouteLocation {
            return RouteLocation(name, Coordinate(latitude, longitude))
        }
    }
}
