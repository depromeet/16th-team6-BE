package com.deepromeet.atcha.transit.domain

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
                else -> null
            }
}
