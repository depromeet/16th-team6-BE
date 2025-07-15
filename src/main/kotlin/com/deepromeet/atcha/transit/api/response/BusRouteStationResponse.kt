package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.bus.BusRouteStation

data class BusRouteStationResponse(
    val busRouteId: String,
    val busRouteName: String,
    val busStationId: String,
    val busStationNumber: String,
    val busStationName: String,
    val busStationLat: Double,
    val busStationLon: Double,
    val order: Int
) {
    constructor(
        busRouteStation: BusRouteStation
    ) : this(
        busRouteId = busRouteStation.busRoute.id.value,
        busRouteName = busRouteStation.busRoute.name,
        busStationId = busRouteStation.busStation.id.value,
        busStationNumber = busRouteStation.busStation.busStationNumber.value,
        busStationName = busRouteStation.busStation.busStationMeta.name,
        busStationLat = busRouteStation.busStation.busStationMeta.coordinate.lat,
        busStationLon = busRouteStation.busStation.busStationMeta.coordinate.lon,
        order = busRouteStation.order
    )
}
