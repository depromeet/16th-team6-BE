package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.RealTimeBusArrival
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Location
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Station
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Step
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

data class LastRoutesResponse(
    val routeId: String,
    val departureDateTime: String,
    val totalTime: Int,
    val totalWalkTime: Int,
    val transferCount: Int,
    val totalDistance: Int,
    val pathType: Int,
    val legs: List<LastRouteLeg>
) {
    fun calculateRemainingTime(): Int {
        return Duration.between(
            LocalDateTime.parse(departureDateTime),
            LocalDateTime.now()
        ).toSeconds().toInt().absoluteValue
    }

    fun isTargetBus(currentBus: RealTimeBusArrival): Boolean {
        val targetBus = legs.firstOrNull { it.mode == "BUS" } ?: return false
        val targetBusDepartureTime = LocalDateTime.parse(targetBus.departureDateTime ?: return false)
        val expectedArrivalTime = currentBus.expectedArrivalTime
        val diffMinutes = ChronoUnit.MINUTES.between(targetBusDepartureTime, expectedArrivalTime)

        // 3) 허용 범위(예: 5분 이내?) 체크
        return kotlin.math.abs(diffMinutes) <= 5
    }
}

data class LastRouteLeg(
    val distance: Int,
    val sectionTime: Int,
    val mode: String,
    val departureDateTime: String? = null,
    val route: String? = null,
    val type: String? = null,
    val service: String? = null,
    val start: Location,
    val end: Location,
    val passStopList: List<Station>? = null,
    val step: List<Step>? = null,
    val passShape: String? = null
)
