package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.deepromeet.atcha.transit.domain.bus.BusRouteId

interface BusPositionFetcher {
    suspend fun fetch(routeId: BusRouteId): List<BusPosition>
}
