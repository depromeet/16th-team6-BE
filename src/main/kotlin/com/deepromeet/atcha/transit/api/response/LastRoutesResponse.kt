package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Location
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Station
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Step
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    fun getRemainingTime(): Int {
        return Duration.between(getDepartureDateTime(), LocalDateTime.now()).toSeconds().toInt()
    }

    private fun getDepartureDateTime(): LocalDateTime {
        val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return LocalDateTime.parse(departureDateTime, dateTimeFormatter)
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
    val Step: List<Step>? = null,
    val passShape: String? = null
)
