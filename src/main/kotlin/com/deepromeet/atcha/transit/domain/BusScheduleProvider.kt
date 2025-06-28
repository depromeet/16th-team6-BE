package com.deepromeet.atcha.transit.domain

interface BusScheduleProvider {
    fun getBusSchedule(routeInfo: BusRouteInfo): BusSchedule?
}
