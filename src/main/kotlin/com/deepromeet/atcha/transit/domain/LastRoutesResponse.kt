package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.api.response.LastRouteLeg

data class LastRoutesResponse(
    val routeId: String,
    val departureDateTime: String,
    val totalTime: Int,
    val totalWalkTime: Int,
    val totalWorkDistance: Int,
    val transferCount: Int,
    val totalDistance: Int,
    val pathType: Int,
    val legs: List<LastRouteLeg>
)
