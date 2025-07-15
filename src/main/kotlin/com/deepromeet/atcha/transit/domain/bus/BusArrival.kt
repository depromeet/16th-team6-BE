package com.deepromeet.atcha.transit.domain.bus

data class BusArrival(
    val schedule: BusSchedule,
    val realTimeArrival: BusRealTimeArrival
)
