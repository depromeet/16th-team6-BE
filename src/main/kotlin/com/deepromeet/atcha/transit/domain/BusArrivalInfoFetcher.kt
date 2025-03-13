package com.deepromeet.atcha.transit.domain

interface BusArrivalInfoFetcher {
    fun getBusArrival(
        station: BusStation,
        route: BusRoute
    ): BusArrival?
}
