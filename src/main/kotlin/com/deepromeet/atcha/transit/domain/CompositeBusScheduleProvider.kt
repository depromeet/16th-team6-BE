package com.deepromeet.atcha.transit.domain

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class CompositeBusScheduleProvider(
    private val providers: List<BusScheduleProvider>
) : BusScheduleProvider {
    override fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule? =
        providers
            .asSequence()
            .mapNotNull { it.getBusSchedule(routeInfo) }
            .firstOrNull()
}
