package com.deepromeet.atcha.transit.domain

object BusTravelTimeCalculator {
    private const val AVERAGE_MINUTE_PER_STATION = 2

    fun calculate(
        busRouteStation: BusRouteStation,
        hasDownTimetable: Boolean
    ): Long {
        val passStationCount =
            when {
                busRouteStation.resolveDirection() == BusDirection.DOWN && hasDownTimetable ->
                    busRouteStation.order - requireNotNull(busRouteStation.turnPoint)
                else -> busRouteStation.order - 1
            }

        return passStationCount * AVERAGE_MINUTE_PER_STATION.toLong()
    }
}
