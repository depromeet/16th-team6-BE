package com.deepromeet.atcha.transit.api.request

data class LastRoutesRequest(
    val startLat: String,
    val startLon: String,
    val endLat: String,
    val endLon: String
)
