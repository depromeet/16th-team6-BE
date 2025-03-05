package com.deepromeet.atcha.transit.domain

interface BusStationInfoClient {
    fun getStationByName(info: BusStationMeta): BusStation?

    fun getRoute(
        station: BusStation,
        routeName: String
    ): BusRoute?
}
