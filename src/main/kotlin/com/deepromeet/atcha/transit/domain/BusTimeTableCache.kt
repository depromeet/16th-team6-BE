package com.deepromeet.atcha.transit.domain

interface BusTimeTableCache {
    fun get(
        routeName: String,
        busStation: BusStationMeta
    ): BusTimeTable?

    fun cache(
        routeName: String,
        busStation: BusStationMeta,
        busTimeTable: BusTimeTable
    )
}
