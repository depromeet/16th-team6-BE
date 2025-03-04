package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.annotation.NoArg
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@NoArg
data class Coordinate(
    val lat: Double,
    val lon: Double
) {
    companion object {
        private const val EARTH_RADIUS_KM = 6371.0 // 지구 반지름 (km)
    }

    fun distanceTo(coordinate: Coordinate): Double {
        val latDistance = Math.toRadians(coordinate.lat - this.lat)
        val lonDistance = Math.toRadians(coordinate.lon - this.lon)

        val a =
            sin(latDistance / 2).pow(2) +
                cos(Math.toRadians(this.lat)) * cos(Math.toRadians(coordinate.lat)) *
                sin(lonDistance / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = EARTH_RADIUS_KM * c

        return distanceKm
    }
}
