package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Step
import java.time.Duration
import java.time.LocalDateTime

data class LastRoutes(
    val routeId: String,
    val departureDateTime: LocalDateTime,
    val totalTime: Int,
    val totalWalkTime: Int,
    val transferCount: Int,
    val totalDistance: Int,
    val pathType: Int,
    val legs: List<LastRouteLeg>
) {
    fun getRemainingTime(): Int {
        return Duration.between(departureDateTime, LocalDateTime.now()).toSeconds().toInt()
    }
}

data class LastRouteLeg(
    val distance: Int,
    val sectionTime: Int,
    val mode: String,
    val departureDateTime: LocalDateTime? = null,
    val route: String? = null,
    val start: LastRouteLocation,
    val end: LastRouteLocation,
    val passStopList: List<LastRouteStation>? = null,
    val step: List<Step>? = null,
    val passShape: String? = null
)

data class LastRouteLocation(
    val stationName: String,
    val coordinate: Coordinate
)

data class LastRouteStation(
    // 순번
    val index: Int,
    // 정류장 명칭
    val stationName: String,
    // 경도
    val lon: String,
    // 위도
    val lat: String
)
