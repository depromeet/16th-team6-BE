package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.mixpanel.event.BusApiCallCountPerRequestProperty
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class CompositeBusScheduleProvider(
    private val providers: List<BusScheduleProvider>
) : BusScheduleProvider {
    override suspend fun getBusSchedule(
        routeInfo: BusRouteInfo,
        busApiCallCountPerRequestProperty: BusApiCallCountPerRequestProperty
    ): BusSchedule? =
        providers
            .asFlow()
            .mapNotNull { it.getBusSchedule(routeInfo, busApiCallCountPerRequestProperty) }
            .firstOrNull()
}
