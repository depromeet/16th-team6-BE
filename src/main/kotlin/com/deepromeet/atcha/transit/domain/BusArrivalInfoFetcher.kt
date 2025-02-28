package com.deepromeet.atcha.transit.domain

interface BusArrivalInfoFetcher {
    fun getBusArrival(
        routeId: RouteId,
        stationId: StationId,
        order: Int
    ): BusArrival
}
