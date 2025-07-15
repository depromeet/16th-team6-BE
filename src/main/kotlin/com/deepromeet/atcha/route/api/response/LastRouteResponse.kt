package com.deepromeet.atcha.route.api.response

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.Station
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Location
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Step

data class LastRouteResponse(
    val routeId: String,
    val departureDateTime: String,
    val totalTime: Int,
    val totalWalkTime: Int,
    val totalWorkDistance: Int,
    val transferCount: Int,
    val totalDistance: Int,
    val pathType: Int,
    val legs: List<LastRouteLegResponse>
) {
    constructor(lastRoute: LastRoute) : this(
        routeId = lastRoute.id,
        departureDateTime = lastRoute.departureDateTime,
        totalTime = lastRoute.totalTime,
        totalWalkTime = lastRoute.totalWalkTime,
        totalWorkDistance = lastRoute.totalWorkDistance,
        transferCount = lastRoute.transferCount,
        totalDistance = lastRoute.totalDistance,
        pathType = lastRoute.pathType,
        legs = lastRoute.legs.map { LastRouteLegResponse(it) }
    )
}

data class LastRouteLegResponse(
    val distance: Int,
    val sectionTime: Int,
    val mode: String,
    val departureDateTime: String? = null,
    val route: String? = null,
    val type: String? = null,
    val service: String? = null,
    val start: Location,
    val end: Location,
    val passStopList: List<Station>? = null,
    val step: List<Step>? = null,
    val passShape: String? = null
) {
    constructor(lastRouteLeg: LastRouteLeg) : this(
        distance = lastRouteLeg.distance,
        sectionTime = lastRouteLeg.sectionTime,
        mode = lastRouteLeg.mode,
        departureDateTime = lastRouteLeg.departureDateTime,
        route = lastRouteLeg.route,
        type = lastRouteLeg.type,
        service = lastRouteLeg.service,
        start = lastRouteLeg.start,
        end = lastRouteLeg.end,
        passStopList = lastRouteLeg.passStopList?.stationList,
        step = lastRouteLeg.step,
        passShape = lastRouteLeg.passShape
    )
}
