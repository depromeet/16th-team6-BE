package com.deepromeet.atcha.transit.domain

interface BusScheduleProvider {
    fun getBusSchedule(
        station: BusStation,
        route: BusRoute
    ): BusSchedule?
}
