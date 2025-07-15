package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.bus.BusRoutePositions
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BusRoutePositionResponse(
    val busRouteStationList: List<BusRouteStationResponse>,
    val turnPoint: Int?,
    val busPositions: List<BusPositionResponse>
) {
    constructor(
        busRoutePositions: BusRoutePositions
    ) : this(
        busRouteStationList = busRoutePositions.routeStations.busRouteStations.map { BusRouteStationResponse(it) },
        turnPoint = busRoutePositions.routeStations.turnPoint,
        busPositions = busRoutePositions.busPositions.map { BusPositionResponse(it) }
    )
}
