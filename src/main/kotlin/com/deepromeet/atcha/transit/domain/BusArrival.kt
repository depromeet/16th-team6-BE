package com.deepromeet.atcha.transit.domain

data class BusArrival(
    val schedule: BusSchedule,
    val realTimeArrival: BusRealTimeArrival
)
