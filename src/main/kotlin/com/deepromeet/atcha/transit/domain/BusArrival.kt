package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class BusArrival(
    val busRoute: BusRoute,
    val busStationId: BusStationId,
    val stationName: String,
    val busTimeTable: BusTimeTable,
    val realTimeInfo: List<RealTimeBusArrival>
) {
    fun getSecondBus(): RealTimeBusArrival? = realTimeInfo.getOrNull(1)
}

data class RealTimeBusArrival(
    val vehicleId: String,
    val busStatus: BusStatus,
    val remainingTime: Int,
    val remainingStations: Int?,
    val isLast: Boolean?,
    val busCongestion: BusCongestion,
    val remainingSeats: Int
) {
    val expectedArrivalTime: LocalDateTime?
        get() =
            when (busStatus) {
                BusStatus.OPERATING, BusStatus.SOON ->
                    LocalDateTime.now()
                        .plusSeconds(remainingTime.toLong())
                else -> null
            }

    fun isTargetBus(targetBus: LastRouteLeg): Boolean {
        val targetBusDepartureTime = LocalDateTime.parse(targetBus.departureDateTime ?: return false)
        val diffMinutes = ChronoUnit.MINUTES.between(targetBusDepartureTime, expectedArrivalTime ?: return false)
        return kotlin.math.abs(diffMinutes) <= 5
    }
}
