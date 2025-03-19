package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.BusRouteDetail
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BusRouteDetailResponse(
    val busRouteStationList: List<BusRouteStationResponse>,
    val turnPoint: Int?,
    val busPositions: List<BusPositionResponse>
) {
    constructor(
        busRouteDetail: BusRouteDetail
    ) : this(
        busRouteStationList = busRouteDetail.routeStations.busRouteStations.map { BusRouteStationResponse(it) },
        turnPoint = busRouteDetail.routeStations.turnPoint,
        busPositions = busRouteDetail.busPositions.map { BusPositionResponse(it) }
    )
}
