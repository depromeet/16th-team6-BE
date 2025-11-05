package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.transit.domain.bus.BusArrival
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import kotlin.takeIf

private const val MAX_EARLY_ARRIVAL_MINUTES = 60

interface RouteArrivalSelectionPolicy {
    fun select(
        scheduledTime: LocalDateTime,
        closestBusInfo: BusArrival?,
        timeTable: BusTimeTable
    ): BusArrival?
}

@Component
class HalfHeadwayPolicyRoute : RouteArrivalSelectionPolicy {
    override fun select(
        scheduledTime: LocalDateTime,
        closestBusInfo: BusArrival?,
        timeTable: BusTimeTable
    ): BusArrival? {
        if (closestBusInfo?.expectedArrivalTime == null) return null

        val closestTime = closestBusInfo.expectedArrivalTime!!
        val diffMin = Duration.between(scheduledTime, closestTime).toMinutes().toDouble()

        return closestBusInfo.takeIf { isValidRefreshedTime(diffMin, timeTable) }
    }

    private fun isValidRefreshedTime(
        diffMin: Double,
        timeTable: BusTimeTable
    ): Boolean {
        val threshold = timeTable.term / 2.0
        return diffMin >= -MAX_EARLY_ARRIVAL_MINUTES && diffMin < threshold
    }
}
