package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.transit.domain.bus.BusArrival
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

interface RouteArrivalSelectionPolicy {
    fun select(
        scheduled: LocalDateTime,
        closestBusInfo: BusArrival?,
        timeTable: BusTimeTable
    ): BusArrival?
}

@Component
class HalfHeadwayPolicyRoute : RouteArrivalSelectionPolicy {
    override fun select(
        scheduled: LocalDateTime,
        closestBusInfo: BusArrival?,
        timeTable: BusTimeTable
    ): BusArrival? {
        if (closestBusInfo?.expectedArrivalTime == null) return null

        val closest = closestBusInfo.expectedArrivalTime!!
        val diffMin = Duration.between(scheduled, closest).toMinutes().toDouble()
        val threshold = timeTable.term / 2.0

        return if (diffMin > threshold) null else closestBusInfo
    }
}
