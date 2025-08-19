package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.shared.infrastructure.mixpanel.event.BusApiCallCountPerRequestProperty
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusSchedule

interface BusScheduleProvider {
    suspend fun getBusSchedule(
        routeInfo: BusRouteInfo,
        busApiCallCountPerRequestProperty: BusApiCallCountPerRequestProperty
    ): BusSchedule?
}
