package com.deepromeet.atcha.transit.domain

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
    override suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule? =
        providers
            .asFlow()
            .mapNotNull { it.getBusSchedule(routeInfo) }
            .firstOrNull()
}
