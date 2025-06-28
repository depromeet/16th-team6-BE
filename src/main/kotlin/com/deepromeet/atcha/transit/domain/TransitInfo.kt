package com.deepromeet.atcha.transit.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TransitInfo.SubwayInfo::class, name = "subway"),
    JsonSubTypes.Type(value = TransitInfo.BusInfo::class, name = "bus"),
    JsonSubTypes.Type(value = TransitInfo.NoInfoTable::class, name = "none")
)
sealed class TransitInfo {
    data class SubwayInfo(val timeTable: SubwayTimeTable) : TransitInfo()

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
