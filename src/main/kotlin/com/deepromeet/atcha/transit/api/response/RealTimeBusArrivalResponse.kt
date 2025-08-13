package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.bus.ArrivalInfoType
import com.deepromeet.atcha.transit.domain.bus.BusArrival
import com.deepromeet.atcha.transit.domain.bus.BusCongestion
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
    val infoType: ArrivalInfoType
) {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    constructor(busArrival: BusArrival) : this(
        busArrival.busStatus,
        busArrival.remainingTime,
        busArrival.remainingStations,
        busArrival.isLast,
        busArrival.busCongestion,
        busArrival.remainingSeats,
        busArrival.expectedArrivalTime?.format(formatter),
        busArrival.vehicleId,
        busArrival.infoType
    )
}
