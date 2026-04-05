package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.subway.SubwayManager
import com.deepromeet.atcha.transit.domain.bus.BusArrival
import com.deepromeet.atcha.transit.domain.subway.SubwayArrival
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
    ): List<SubwayArrival>? {
        val subwayInfo = targetSubway.requireSubwayInfo()
        val stationName = subwayInfo.timeTable.startStation.name
        val subwayLine = subwayInfo.subwayLine
        val direction = subwayInfo.timeTable.subwayDirection

        val arrivals =
            subwayManager.getRealTimeSubwayArrivals(
                stationName = stationName,
                subwayLine = subwayLine,
                direction = direction
            )

        return arrivals.getClosestArrivals(scheduled)
    }
}
