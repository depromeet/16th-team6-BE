package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.bus.BusCongestion
import com.deepromeet.atcha.transit.domain.bus.BusInfoType
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeInfo
import com.deepromeet.atcha.transit.domain.bus.BusStatus
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
    val vehicleId: String?,
    val infoType: BusInfoType
) {
    constructor(busRealTimeInfo: BusRealTimeInfo) : this(
        busRealTimeInfo.busStatus,
        busRealTimeInfo.remainingTimeExtra,
        busRealTimeInfo.remainingStations,
        busRealTimeInfo.isLast,
        busRealTimeInfo.busCongestion,
        busRealTimeInfo.remainingSeats,
        busRealTimeInfo.expectedArrivalTime?.toString(),
        busRealTimeInfo.vehicleId,
        busRealTimeInfo.infoType
    )
}
