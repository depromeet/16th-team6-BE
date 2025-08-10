package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.bus.BusCongestion
import com.deepromeet.atcha.transit.domain.bus.BusInfoType
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeInfo
import com.deepromeet.atcha.transit.domain.bus.BusStatus
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.format.DateTimeFormatter

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
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    constructor(busRealTimeInfo: BusRealTimeInfo) : this(
        busRealTimeInfo.busStatus,
        busRealTimeInfo.remainingTime,
        busRealTimeInfo.remainingStations,
        busRealTimeInfo.isLast,
        busRealTimeInfo.busCongestion,
        busRealTimeInfo.remainingSeats,
        busRealTimeInfo.expectedArrivalTime?.format(formatter),
        busRealTimeInfo.vehicleId,
        busRealTimeInfo.infoType
    )
}
