package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStation
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwayTime
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable

sealed class TransitInfo {
    data class SubwayInfo(
        val subwayLine: SubwayLine,
        val timeTable: SubwayTimeTable,
        val targetSchedule: SubwayTime
    ) : TransitInfo() {
        fun resolveFinalStationName(): String = targetSchedule.finalStation.name

        fun resolveDirectionName(): String = targetSchedule.subwayDirection.getName(subwayLine.isCircular)
    }

    data class BusInfo(
        val busRoute: BusRoute,
        val busStation: BusStation,
        val timeTable: BusTimeTable
    ) : TransitInfo() {
        constructor(
            busSchedule: BusSchedule
        ) : this(
            busRoute = busSchedule.busRoute,
            busStation = busSchedule.busStation,
            timeTable = busSchedule.busTimeTable
        )
    }

    data object NoInfoTable : TransitInfo()
}
