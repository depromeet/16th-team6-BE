package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.subway.SubwayArrival
import com.deepromeet.atcha.transit.domain.subway.SubwayArrivalStatus
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.format.DateTimeFormatter

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RealTimeSubwayArrivalResponse(
    val routeName: String,
    val subwayArrivalStatus: SubwayArrivalStatus,
    val remainingTime: Int,
    val remainingStations: Int?,
    val isLast: Boolean,
    val expectedArrivalTime: String?,
    val trainNo: String,
    val destination: String,
    val direction: String
) {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    constructor(routeName: String, subwayArrival: SubwayArrival) : this(
        routeName = routeName,
        subwayArrivalStatus = subwayArrival.arrivalStatus,
        remainingTime = subwayArrival.remainingTimeSeconds,
        remainingStations = subwayArrival.remainingStations,
        isLast = subwayArrival.isLast,
        expectedArrivalTime = subwayArrival.expectedArrivalTime.format(formatter),
        trainNo = subwayArrival.trainNo,
        destination = subwayArrival.destination,
        direction = subwayArrival.direction
    )
}
