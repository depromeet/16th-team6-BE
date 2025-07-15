package com.deepromeet.atcha.transit.domain.bus

import java.time.LocalDateTime

data class BusSchedule(
    val busRoute: BusRoute,
    val busStation: BusStation,
    val busTimeTable: BusTimeTable
)

data class BusRealTimeInfo(
    val vehicleId: String,
    val busStatus: BusStatus,
    val remainingTime: Int,
    val remainingStations: Int?,
    val isLast: Boolean?,
    val busCongestion: BusCongestion,
    val remainingSeats: Int?
) {
    val expectedArrivalTime: LocalDateTime?
        get() =
            when (busStatus) {
                BusStatus.OPERATING, BusStatus.SOON ->
                    LocalDateTime.now()
                        .plusSeconds(remainingTime.toLong())
                        .minusSeconds(30)
                else -> null
            }

    val remainingTimeExtra: Int
        get() = remainingTime - 30
}
