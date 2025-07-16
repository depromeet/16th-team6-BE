package com.deepromeet.atcha.transit.api.request

data class RoutePassStopRequest(
    val index: Int,
    val stationName: String,
    val lon: String,
    val lat: String
)
