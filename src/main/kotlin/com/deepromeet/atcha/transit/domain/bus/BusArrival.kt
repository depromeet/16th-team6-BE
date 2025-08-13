package com.deepromeet.atcha.transit.domain.bus

import java.time.Duration
import java.time.LocalDateTime

enum class ArrivalInfoType {
    REALTIME,
    ESTIMATED
}

data class BusArrival(
    val vehicleId: String,
    val busStatus: BusStatus,
    val remainingTime: Int,
    val remainingStations: Int?,
    val isLast: Boolean?,
    val busCongestion: BusCongestion?,
    val remainingSeats: Int?,
    val infoType: ArrivalInfoType = ArrivalInfoType.REALTIME
) {
    val expectedArrivalTime: LocalDateTime?
        get() =
            when (busStatus) {
                BusStatus.OPERATING, BusStatus.SOON ->
                    LocalDateTime.now()
                        .plusSeconds(remainingTime.toLong())
                else -> null
            }

    companion object {
        fun createScheduled(scheduledTime: LocalDateTime): BusArrival {
            val remainingSeconds =
                Duration.between(LocalDateTime.now(), scheduledTime)
                    .seconds.coerceAtLeast(0).toInt()

            return BusArrival(
                vehicleId = "scheduled",
                busStatus = BusStatus.OPERATING,
                remainingTime = remainingSeconds,
                remainingStations = null,
                isLast = false,
                busCongestion = null,
                remainingSeats = null,
                infoType = ArrivalInfoType.ESTIMATED
            )
        }

        fun createEstimated(
            vehicleId: String,
            estimatedArrivalTime: LocalDateTime,
            busCongestion: BusCongestion?,
            remainingSeats: Int?
        ): BusArrival {
            val remainingSeconds =
                Duration.between(LocalDateTime.now(), estimatedArrivalTime)
                    .seconds.coerceAtLeast(0).toInt()

            return BusArrival(
                vehicleId = vehicleId,
                busStatus = BusStatus.OPERATING,
                remainingTime = remainingSeconds,
                remainingStations = null,
                isLast = false,
                busCongestion = busCongestion,
                remainingSeats = remainingSeats,
                infoType = ArrivalInfoType.ESTIMATED
            )
        }
    }
}
