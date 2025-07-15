package com.deepromeet.atcha.transit.domain.route

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Location
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.PassStopList
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Step
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.absoluteValue

data class LastRoute(
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

    fun findFirstBus(): LastRouteLeg {
        return legs.first { it.mode == "BUS" }
    }

    fun findFirstTransit(): LastRouteLeg {
        return legs.first { it.mode == "BUS" || it.mode == "SUBWAY" }
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
    val passStopList: PassStopList? = null,
    val step: List<Step>? = null,
    val passShape: String? = null,
    val transitInfo: TransitInfo
) {
    fun resolveRouteName(): String {
        return route!!.split(":")[1]
    }

    fun toBusStationMeta(): BusStationMeta {
        return BusStationMeta(
            start.name,
            Coordinate(start.lat, start.lon)
        )
    }

    fun isBus(): Boolean {
        return mode == "BUS"
    }

    fun isSubway(): Boolean {
        return mode == "SUBWAY"
    }
}

fun List<LastRoute>.sort(sortType: LastRouteSortType): List<LastRoute> {
    val now = LocalDateTime.now()
    val upcomingRoutes =
        this.filter {
            LocalDateTime.parse(it.departureDateTime).isAfter(now)
        }

    return when (sortType) {
        LastRouteSortType.MINIMUM_TRANSFERS ->
            upcomingRoutes.sortedWith(
                compareBy({ it.transferCount }, { it.totalTime })
            )
        LastRouteSortType.DEPARTURE_TIME_DESC -> upcomingRoutes.sortedByDescending { it.departureDateTime }
    }
}

data class Station(
    val index: Int,
    val stationName: String,
    val lon: String,
    val lat: String
)
