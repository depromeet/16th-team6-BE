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
        val (patchedStart, patchedPassStops) = patchStartForBus(transitInfo)

        return LastRouteLeg(
            distance = distance,
            sectionTime = sectionTime,
            mode = mode,
            departureDateTime = departureDateTime,
            route = route,
            type = type,
            service = service,
            start = patchedStart,
            end = end,
            steps = steps,
            passStops = patchedPassStops,
            pathCoordinates = pathCoordinates,
            transitInfo = transitInfo
        )
    }

    private fun patchStartForBus(transitInfo: TransitInfo): Pair<RouteLocation, RoutePassStops?> {
        if (mode != RouteMode.BUS || transitInfo !is TransitInfo.BusInfo) {
            return start to passStops
        }

        val realStationName = transitInfo.busStation.busStationMeta.name

        val newStart =
            if (start.name != realStationName) {
                start.copy(name = realStationName)
            } else {
                start
            }

        val newPassStops =
            passStops?.let { ps ->
                if (ps.stops.isNotEmpty() && ps.stops.first().stationName != realStationName) {
                    val fixedFirst =
                        ps.stops.first().copy(
                            stationName = realStationName
                        )
                    RoutePassStops(listOf(fixedFirst) + ps.stops.drop(1))
                } else {
                    ps
                }
            }

        return newStart to newPassStops
    }

    companion object {
        private const val WALK_TIME_BUFFER_SECONDS = 120
    }
}
