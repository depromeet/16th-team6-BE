package com.deepromeet.atcha.transit.application.bus

import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta

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
