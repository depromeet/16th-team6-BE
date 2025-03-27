package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Location
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Station
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Step
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

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
) {
    fun calculateRemainingTime(): Int {
        return Duration.between(
            LocalDateTime.parse(departureDateTime),
            LocalDateTime.now()
        ).toSeconds().toInt().absoluteValue
    }

    fun getFirstBus(): LastRouteLeg {
        return legs.first { it.mode == "BUS" }
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
) {
    fun resolveRouteName(): String {
        return route!!.split(":")[1]
    }

    fun resolveStartStation(): BusStationMeta {
        return BusStationMeta(
            start.name,
            Coordinate(start.lat, start.lon)
        )
    }
}
