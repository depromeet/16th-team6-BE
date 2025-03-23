package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.LastRouteSortType

data class LastRoutesRequest(
    val startLat: String,
    val startLon: String,
    val endLat: String?,
    val endLon: String?,
    val sortType: LastRouteSortType = LastRouteSortType.MINIMUM_TRANSFERS
) {
    fun toStart(): Coordinate = Coordinate(startLat.toDouble(), startLon.toDouble())
}
