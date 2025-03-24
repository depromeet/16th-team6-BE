package com.deepromeet.atcha.transit.domain

data class BusRouteStation(
    val busRoute: BusRoute,
    val busStation: BusStation,
    val order: Int
)
