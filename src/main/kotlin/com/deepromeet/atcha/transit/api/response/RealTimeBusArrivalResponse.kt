package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusStatus
import com.deepromeet.atcha.transit.domain.RealTimeBusArrival
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RealTimeBusArrivalResponse(
    val busStatus: BusStatus,
    val remainingTime: Int,
    val remainingStations: Int?,
    val isLast: Boolean?,
    val busCongestion: BusCongestion?,
    val remainingSeats: Int?,
    val expectedArrivalTime: String?,
    val vehicleId: String?
) {
    constructor(realTimeBusArrival: RealTimeBusArrival) : this(
        realTimeBusArrival.busStatus,
        realTimeBusArrival.remainingTime,
        realTimeBusArrival.remainingStations,
        realTimeBusArrival.isLast,
        realTimeBusArrival.busCongestion,
        realTimeBusArrival.remainingSeats,
        realTimeBusArrival.expectedArrivalTime?.toString(),
        realTimeBusArrival.vehicleId
    )
}
