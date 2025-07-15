package com.deepromeet.atcha.transit.domain.bus

data class BusRouteStation(
    val busRoute: BusRoute,
    val busStation: BusStation,
    val order: Int,
    val turnPoint: Int? = null
) {
    val stationId: String
        get() = busStation.id.value
    val stationNumber: String
        get() = busStation.busStationNumber.value
    val stationName: String
        get() = busStation.busStationMeta.name

    fun resolveDirection(): BusDirection {
        return if (turnPoint == null || order < turnPoint) {
            BusDirection.UP
        } else {
            BusDirection.DOWN
        }
    }
}
