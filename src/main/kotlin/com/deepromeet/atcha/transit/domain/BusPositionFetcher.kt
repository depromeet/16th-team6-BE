package com.deepromeet.atcha.transit.domain

interface BusPositionFetcher {
    fun fetch(routeId: BusRouteId): List<BusPosition>
}
