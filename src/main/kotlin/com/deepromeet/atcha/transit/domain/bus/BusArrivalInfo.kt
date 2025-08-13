package com.deepromeet.atcha.transit.domain.bus

data class BusArrivalInfo(
    val schedule: BusSchedule,
    val realTimeArrival: BusRealTimeArrivals
)
