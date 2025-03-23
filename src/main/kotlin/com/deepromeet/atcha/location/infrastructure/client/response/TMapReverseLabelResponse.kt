package com.deepromeet.atcha.location.infrastructure.client.response

data class TMapReverseLabelResponse(
    val poiInfo: PoiInfo
) {
    data class PoiInfo(
        val name: String,
        val poiLat: String,
        val poiLon: String
    )
}
