package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate

data class LastRoutesRequest(
    val startLat: String,
    val startLon: String,
    val endLat: String?,
    val endLon: String?
) {
    fun toStart(): Coordinate = Coordinate(startLat.toDouble(), startLon.toDouble())
}
