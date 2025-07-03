package com.deepromeet.atcha.transit.domain

interface BusScheduleProvider {
    suspend fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule?
}
