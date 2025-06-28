package com.deepromeet.atcha.transit.domain

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
        get() = busStation.busStationMeta.resolveName()

    fun getDirection(): BusDirection {
        return if (turnPoint == null || order < turnPoint) {
            BusDirection.UP
        } else {
            BusDirection.DOWN
        }
    }
}
