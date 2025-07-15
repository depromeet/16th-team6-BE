package com.deepromeet.atcha.transit.domain.bus

interface BusPositionFetcher {
    suspend fun fetch(routeId: BusRouteId): List<BusPosition>
}
