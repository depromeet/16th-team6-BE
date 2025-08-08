package com.deepromeet.atcha.transit.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.api.request.RoutePassStopRequest
import com.deepromeet.atcha.transit.domain.RoutePassStop
import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta

data class BusArrivalRequest(
    val routeName: String,
    val stationName: String,
    val passStations: List<RoutePassStopRequest>,
    val lat: Double,
    val lon: Double
) {
    fun toBusStationMeta() =
        BusStationMeta(
            stationName,
            Coordinate(lat, lon)
        )

    fun toRouteName(): String {
        return routeName.split(":")[1]
    }

    fun toRoutePassStops(): RoutePassStops {
        return RoutePassStops(
            passStations.map { RoutePassStop.of(it.index, it.stationName, it.lat.toDouble(), it.lon.toDouble()) }
        )
    }
}
