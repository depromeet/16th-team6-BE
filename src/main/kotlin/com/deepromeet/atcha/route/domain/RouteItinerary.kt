package com.deepromeet.atcha.route.domain

data class RouteItinerary(
    val totalTime: Int,
    val transferCount: Int,
    val totalWalkDistance: Int,
    val totalDistance: Int,
    val totalWalkTime: Int,
    val totalFare: Double,
    val legs: List<RouteLeg>,
    val pathType: Int
)
