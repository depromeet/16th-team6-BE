package com.deepromeet.atcha.transit.api.request

data class BusArrivalRequest(
    val routeName: String,
    val stationName: String,
    val lat: Double,
    val lon: Double
)
