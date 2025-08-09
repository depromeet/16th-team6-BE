package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.route.domain.RouteLocation

data class RoutePassStop(
    val index: Int,
    val location: RouteLocation,
    val stationName: String
) {
    companion object {
        fun of(
            index: Int,
            stationName: String,
            latitude: Double,
            longitude: Double
        ): RoutePassStop {
            return RoutePassStop(
                index = index,
                location = RouteLocation.Companion.of(stationName, latitude, longitude),
                stationName = stationName
            )
        }
    }
}
