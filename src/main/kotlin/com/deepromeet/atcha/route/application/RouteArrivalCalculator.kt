package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.subway.SubwayManager
import com.deepromeet.atcha.transit.domain.bus.BusArrival
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class RouteArrivalCalculator(
    private val busManager: BusManager,
    private val subwayManager: SubwayManager
) {
    suspend fun closestArrivals(
        targetBus: LastRouteLeg,
        scheduled: LocalDateTime
    ): List<BusArrival>? {
        val busInfo = targetBus.requireBusInfo()
        val arrivals =
            busManager.getRealTimeArrival(
                targetBus.resolveRouteName(),
                targetBus.toBusStationMeta(),
                targetBus.passStops!!
            )
        val positions = busManager.getBusPositions(busInfo.busRouteInfo.route)
        val approachingBuses = positions.getApproachingBuses(busInfo.busStation)

        return arrivals.getClosestArrivalsWithPositions(busInfo, scheduled, approachingBuses)
    }

    suspend fun closestSubwayArrivals(
        targetSubway: LastRouteLeg,
        scheduled: LocalDateTime
    ): List<BusArrival>? {
        val subwayInfo = targetSubway.requireSubwayInfo()
        val arrivals =
            subwayManager.getRealTimeArrival(
                targetSubway.resolveRouteName(),
                targetSubway.toBusStationMeta(),
                targetSubway.passStops!!
            )
        val positions = subwayManager.getBusPositions(subwayInfo.busRouteInfo.route)
        val approachingBuses = positions.getApproachingBuses(subwayInfo.busStation)

        return arrivals.getClosestSubwayArrivalsWithPositions(subwayInfo, scheduled, approachingBuses)
    }
}
