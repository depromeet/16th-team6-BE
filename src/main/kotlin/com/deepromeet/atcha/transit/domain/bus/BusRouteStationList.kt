package com.deepromeet.atcha.transit.domain.bus

data class BusRouteStationList(
    val busRouteStations: List<BusRouteStation>,
    val turnPoint: Int?
) {
    fun getTargetStationById(busStationId: BusStationId): BusRouteStation? {
        return busRouteStations.firstOrNull { it.busStation.id == busStationId }
    }
}
