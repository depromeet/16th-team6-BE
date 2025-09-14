package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStation
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.domain.subway.SubwaySchedule
import com.deepromeet.atcha.transit.domain.subway.SubwayTimeTable

sealed class TransitInfo {
    data class SubwayInfo(
        val subwayLine: SubwayLine,
        val timeTable: SubwayTimeTable,
        val lastSchedule: SubwaySchedule,
        val isExpress: Boolean
    ) : TransitInfo() {
        fun resolveFinalStationName(): String = lastSchedule.finalStation.name

        fun resolveDirectionName(): String = lastSchedule.subwayDirection.getName(subwayLine.isCircular)
    }

    data class BusInfo(
        val busRouteInfo: BusRouteInfo,
        val busStation: BusStation,
        val timeTable: BusTimeTable
    ) : TransitInfo() {
        constructor(
            busSchedule: BusSchedule
        ) : this(
            busRouteInfo = busSchedule.busRouteInfo,
            busStation = busSchedule.busRouteInfo.targetStation.busStation,
            timeTable = busSchedule.busTimeTable
        )
    }

    data object NoInfoTable : TransitInfo()
}
