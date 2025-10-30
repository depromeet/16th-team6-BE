package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import java.time.LocalDateTime

data class RouteLeg(
    val distance: Int,
    val sectionTime: Int,
    val mode: RouteMode,
    val route: String?,
    val routeColor: String?,
    val type: String?,
    val service: String?,
    val start: RouteLocation,
    val end: RouteLocation,
    val steps: List<RouteStep>?,
    val passStops: RoutePassStops?,
    val pathCoordinates: String?
) {
    fun isExpress(): Boolean = route?.contains("(급행)") == true

    fun resolveRouteName(): String {
        return route?.split(":")?.getOrNull(1) ?: route ?: ""
    }

    fun toBusStationMeta(): BusStationMeta {
        return BusStationMeta(
            start.name,
            start.coordinate
        )
    }

    fun toLastWalkLeg(): LastRouteLeg? {
        if (sectionTime == 0) return null
        return LastRouteLeg(
            distance = this.distance,
            sectionTime = this.sectionTime + WALK_TIME_BUFFER_SECONDS,
            mode = this.mode,
            departureDateTime = null,
            route = this.route,
            type = this.type,
            service = this.service,
            start = this.start,
            end = this.end,
            steps = this.steps,
            passStops = this.passStops,
            pathCoordinates = this.pathCoordinates,
            transitInfo = TransitInfo.NoInfoTable
        )
    }

    fun toLastTransitLeg(
        departureDateTime: LocalDateTime,
        transitInfo: TransitInfo
    ): LastRouteLeg {
        val adjustedSectionTime =
            if (mode == RouteMode.BUS) {
                (sectionTime * BUS_TIME_REDUCTION_RATE).toInt()
            } else {
                sectionTime
            }

        return LastRouteLeg(
            distance = distance,
            sectionTime = adjustedSectionTime,
            mode = mode,
            departureDateTime = departureDateTime,
            route = route,
            type = type,
            service = service,
            start = start,
            end = end,
            steps = steps,
            passStops = passStops,
            pathCoordinates = pathCoordinates,
            transitInfo = transitInfo
        )
    }

    companion object {
        private const val WALK_TIME_BUFFER_SECONDS = 120
        private const val BUS_TIME_REDUCTION_RATE = 0.7
    }
}
