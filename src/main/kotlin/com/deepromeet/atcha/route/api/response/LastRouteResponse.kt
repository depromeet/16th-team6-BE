package com.deepromeet.atcha.route.api.response

import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.RouteLocation
import com.deepromeet.atcha.route.domain.RouteStep
import com.deepromeet.atcha.transit.domain.RoutePassStop
import com.deepromeet.atcha.transit.domain.bus.BusStation
import java.time.format.DateTimeFormatter

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
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    constructor(lastRoute: LastRoute) : this(
        routeId = lastRoute.id,
        departureDateTime = lastRoute.departureDateTime.format(dateTimeFormatter),
        totalTime = lastRoute.totalTime,
        totalWalkTime = lastRoute.totalWalkTime,
        totalWorkDistance = lastRoute.totalWalkDistance,
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
    val start: RouteLocationResponse,
    val end: RouteLocationResponse,
    val targetBusStation: BusStationResponse? = null,
    val targetBusTerm: Int? = null,
    val subwayFinalStation: String? = null,
    val subwayDirection: String? = null,
    val isExpressSubway: Boolean? = null,
    val isLastSubway: Boolean? = null,
    val passStopList: List<RoutePassStopResponse>? = null,
    val step: List<RouteStep>? = null,
    val passShape: String? = null
) {
    companion object {
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }

    constructor(lastRouteLeg: LastRouteLeg) : this(
        distance = lastRouteLeg.distance,
        sectionTime = lastRouteLeg.sectionTime,
        mode = lastRouteLeg.mode.value,
        departureDateTime = lastRouteLeg.departureDateTime?.format(dateTimeFormatter),
        route = lastRouteLeg.route,
        type = lastRouteLeg.type,
        service = lastRouteLeg.service,
        start = RouteLocationResponse(lastRouteLeg.start),
        end = RouteLocationResponse(lastRouteLeg.end),
        targetBusStation =
            lastRouteLeg.busInfo?.busRouteInfo?.targetStation?.busStation?.let {
                BusStationResponse(
                    it
                )
            },
        targetBusTerm = lastRouteLeg.busInfo?.timeTable?.term,
        subwayFinalStation = lastRouteLeg.subwayInfo?.resolveFinalStationName(),
        subwayDirection = lastRouteLeg.subwayInfo?.resolveDirectionName(),
        isExpressSubway = lastRouteLeg.subwayInfo?.isExpress,
        isLastSubway =
            lastRouteLeg.subwayInfo?.lastSchedule?.departureTime?.toLocalDateTime()?.isEqual(
                lastRouteLeg.departureDateTime
            ),
        passStopList = lastRouteLeg.passStops?.stops?.map { RoutePassStopResponse(it) },
        step = lastRouteLeg.steps,
        passShape = lastRouteLeg.pathCoordinates
    )
}

data class RouteLocationResponse(
    val lat: Double,
    val lon: Double,
    val name: String
) {
    constructor(
        routeLocation: RouteLocation
    ) : this(
        routeLocation.latitude,
        routeLocation.longitude,
        routeLocation.name
    )
}

data class RoutePassStopResponse(
    val index: Int,
    val stationName: String,
    val lon: String,
    val lat: String
) {
    constructor(
        routePassStop: RoutePassStop
    ) : this(
        routePassStop.index,
        routePassStop.stationName,
        routePassStop.location.longitude.toString(),
        routePassStop.location.latitude.toString()
    )
}

data class BusStationResponse(
    val busStationId: String,
    val busStationNumber: String,
    val busStationName: String
) {
    constructor(
        busStation: BusStation
    ) : this(
        busStation.id.value,
        busStation.busStationNumber.value,
        busStation.busStationMeta.name
    )
}
