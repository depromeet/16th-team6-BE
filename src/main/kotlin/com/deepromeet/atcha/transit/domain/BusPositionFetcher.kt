package com.deepromeet.atcha.transit.domain

interface BusPositionFetcher {
    suspend fun fetch(routeId: BusRouteId): List<BusPosition>
}
