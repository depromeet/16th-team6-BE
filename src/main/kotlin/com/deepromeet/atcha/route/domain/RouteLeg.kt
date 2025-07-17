package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta

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
    val pathCoordinates: String?,
    val transitInfo: TransitInfo
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

    fun toLastWalkLeg() =
        LastRouteLeg(
            distance = this.distance,
            sectionTime = this.sectionTime,
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
            transitInfo = transitInfo
        )

    fun toLastTransitLeg(
        departureDateTime: String,
        transitInfo: TransitInfo
    ): LastRouteLeg {
        return LastRouteLeg(
            distance = this.distance,
            sectionTime = this.sectionTime,
            mode = this.mode,
            departureDateTime = departureDateTime,
            route = this.route,
            type = this.type,
            service = this.service,
            start = this.start,
            end = this.end,
            steps = this.steps,
            passStops = this.passStops,
            pathCoordinates = this.pathCoordinates,
            transitInfo = transitInfo
        )
    }
}
