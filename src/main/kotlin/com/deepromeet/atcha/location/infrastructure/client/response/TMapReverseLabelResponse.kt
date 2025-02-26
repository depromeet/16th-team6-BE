package com.deepromeet.atcha.location.infrastructure.client.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location

data class TMapReverseLabelResponse(
    val poiInfo: PoiInfo
) {
    fun toLocation(): Location {
        return Location(
            poiInfo.name,
            Coordinate(poiInfo.poiLat.toDouble(), poiInfo.poiLon.toDouble())
        )
    }

    data class PoiInfo(
        val name: String,
        val poiLat: String,
        val poiLon: String
    )
}
