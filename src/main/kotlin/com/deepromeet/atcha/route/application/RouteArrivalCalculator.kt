package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class RouteArrivalCalculator(
    private val busManager: BusManager
) {
    suspend fun closestArrival(
        timeTable: BusTimeTable,
        scheduled: LocalDateTime,
        routeName: String,
        stationMeta: BusStationMeta,
        passStops: RoutePassStops,
        busInfo: TransitInfo.BusInfo
    ): BusRealTimeInfo? {
        val arrivals = busManager.getRealTimeArrival(routeName, stationMeta, passStops)
        val positions = busManager.getBusPositions(busInfo.busRoute)
        val approachingBuses = positions.getApproachingBuses(busInfo.busStation)

        return arrivals.getClosestArrivalWithPositions(timeTable, scheduled, approachingBuses)
    }
}
