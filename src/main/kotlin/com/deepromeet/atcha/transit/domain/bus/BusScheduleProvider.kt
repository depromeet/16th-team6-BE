package com.deepromeet.atcha.transit.domain.bus

interface BusScheduleProvider {
    suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule?
}
