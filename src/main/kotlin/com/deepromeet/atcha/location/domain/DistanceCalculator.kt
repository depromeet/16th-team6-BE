package com.deepromeet.atcha.location.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceCalculator {
    private const val EARTH_RADIUS_KM = 6371.0 // 지구 반지름 (km)

    fun calculate(
        pois: List<POI>,
        currentCoordinate: Coordinate
    ): List<POI> {
        return pois.map { poi ->
            poi.copy(radius = haversine(poi.location.coordinate, currentCoordinate))
        }
    }

    private fun haversine(
        targetCoordinate: Coordinate,
        currentCoordinate: Coordinate
    ): Int {
        val latDistance = Math.toRadians(targetCoordinate.lat - currentCoordinate.lat)
        val lonDistance = Math.toRadians(targetCoordinate.lon - currentCoordinate.lon)

        val a =
            sin(latDistance / 2).pow(2) +
                cos(Math.toRadians(currentCoordinate.lat)) * cos(Math.toRadians(targetCoordinate.lat)) *
                sin(lonDistance / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = EARTH_RADIUS_KM * c // 거리 (km)

        return distanceKm.toInt() // Int로 변환하여 반환
    }
}
