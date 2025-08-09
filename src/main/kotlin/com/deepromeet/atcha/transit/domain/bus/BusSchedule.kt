package com.deepromeet.atcha.transit.domain.bus

import java.time.LocalDateTime

enum class BusInfoType {
    REALTIME,
    ESTIMATED
}

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
    val busCongestion: BusCongestion?,
    val remainingSeats: Int?,
    val infoType: BusInfoType = BusInfoType.REALTIME
) {
    val expectedArrivalTime: LocalDateTime?
        get() =
            when (busStatus) {
                BusStatus.OPERATING, BusStatus.SOON ->
                    LocalDateTime.now()
                        .plusSeconds(remainingTime.toLong())
                else -> null
            }

    val remainingTimeExtra: Int
        get() = remainingTime - 30

    companion object {
        fun createScheduled(scheduledTime: LocalDateTime): BusRealTimeInfo {
            val remainingSeconds =
                java.time.Duration.between(LocalDateTime.now(), scheduledTime)
                    .seconds.coerceAtLeast(0).toInt()

            return BusRealTimeInfo(
                vehicleId = "scheduled",
                busStatus = BusStatus.OPERATING,
                remainingTime = remainingSeconds,
                remainingStations = null,
                isLast = false,
                busCongestion = null,
                remainingSeats = null,
                infoType = BusInfoType.ESTIMATED
            )
        }

        fun createEstimated(
            vehicleId: String,
            estimatedArrivalTime: LocalDateTime,
            busCongestion: BusCongestion?,
            remainingSeats: Int?
        ): BusRealTimeInfo {
            val remainingSeconds =
                java.time.Duration.between(LocalDateTime.now(), estimatedArrivalTime)
                    .seconds.coerceAtLeast(0).toInt()

            return BusRealTimeInfo(
                vehicleId = vehicleId,
                busStatus = BusStatus.OPERATING,
                remainingTime = remainingSeconds,
                remainingStations = null,
                isLast = false,
                busCongestion = busCongestion,
                remainingSeats = remainingSeats,
                infoType = BusInfoType.ESTIMATED
            )
        }
    }
}
