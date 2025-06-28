package com.deepromeet.atcha.transit.domain

interface BusTimeTableCache {
    fun get(
        routeName: String,
        busStation: BusStationMeta
    ): BusSchedule?

    fun cache(
        routeName: String,
        busStation: BusStationMeta,
        busSchedule: BusSchedule
    )
}
