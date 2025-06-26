package com.deepromeet.atcha.transit.domain

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class CompositeBusScheduleProvider(
    private val providers: List<BusScheduleProvider>
) : BusScheduleProvider {
    override fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule? =
        providers
            .asSequence()
            .mapNotNull { it.getBusSchedule(station, route) }
            .firstOrNull()
}
