package com.deepromeet.atcha.transit.domain

data class BusRouteStationList(
    val busRouteStations: List<BusRouteStation>,
    val turnPoint: Int?
) {
    fun getStationById(stationId: BusStationId): BusRouteStation? {
        return busRouteStations.firstOrNull { it.busStation.id == stationId }
    }

    fun getStationByName(stationName: String): BusRouteStation? {
        return busRouteStations.firstOrNull { it.busStation.busStationMeta.resolveName() == stationName }
    }

    fun getDirectionByIndex(index: Int): BusDirection {
        return busRouteStations[index].getDirection()
    }
}
