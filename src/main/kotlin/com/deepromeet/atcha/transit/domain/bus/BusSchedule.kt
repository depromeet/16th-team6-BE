package com.deepromeet.atcha.transit.domain.bus

data class BusSchedule(
    val busRouteInfo: BusRouteInfo,
    val busStation: BusStation,
    val busTimeTable: BusTimeTable
)

