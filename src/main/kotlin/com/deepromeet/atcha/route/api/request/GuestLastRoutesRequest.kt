package com.deepromeet.atcha.route.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class GuestLastRoutesRequest(
    val startLat: String,
    val startLon: String,
    val endLat: String,
    val endLon: String
) {
    fun toStart(): Coordinate = Coordinate(startLat.toDouble(), startLon.toDouble())

    fun toEnd(): Coordinate = Coordinate(endLat.toDouble(), endLon.toDouble())
}
