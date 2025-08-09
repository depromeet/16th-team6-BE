package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.transit.domain.bus.BusRealTimeInfo
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

interface ArrivalSelectionPolicy {
    fun select(
        timeTable: BusTimeTable,
        scheduled: LocalDateTime,
        closestBusInfo: BusRealTimeInfo?
    ): BusRealTimeInfo?
}

@Component
class HalfHeadwayPolicy : ArrivalSelectionPolicy {
    override fun select(
        timeTable: BusTimeTable,
        scheduled: LocalDateTime,
        closestBusInfo: BusRealTimeInfo?
    ): BusRealTimeInfo? {
        if (closestBusInfo?.expectedArrivalTime == null) return null

        val closest = closestBusInfo.expectedArrivalTime!!
        val diffMin = Duration.between(scheduled, closest).toMinutes().toDouble()
        val threshold = timeTable.term / 2.0

        return if (diffMin > threshold) null else closestBusInfo
    }
}
