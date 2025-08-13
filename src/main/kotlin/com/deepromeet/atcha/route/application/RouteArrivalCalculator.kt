package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.domain.bus.BusArrival
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class RouteArrivalCalculator(
    private val busManager: BusManager
) {
    suspend fun closestArrival(
        busLeg: LastRouteLeg,
        scheduled: LocalDateTime
    ): BusArrival? {
        val busInfo = busLeg.requireBusInfo()
        val arrivals =
            busManager.getRealTimeArrival(
                busLeg.resolveRouteName(),
                busLeg.toBusStationMeta(),
                busLeg.passStops!!
            )
        val positions = busManager.getBusPositions(busInfo.busRouteInfo.route)
        val approachingBuses = positions.getApproachingBuses(busInfo.busStation)

        return arrivals.getClosestArrivalWithPositions(busInfo.timeTable, scheduled, approachingBuses)
    }
}
