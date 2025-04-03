package com.deepromeet.atcha.transit.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TransitTime.SubwayTimeInfo::class, name = "subway"),
    JsonSubTypes.Type(value = TransitTime.BusTimeInfo::class, name = "bus"),
    JsonSubTypes.Type(value = TransitTime.NoTimeTable::class, name = "none")
)
sealed class TransitTime {
    data class SubwayTimeInfo(val timeTable: SubwayTimeTable) : TransitTime()

    data class BusTimeInfo(val arrivalInfo: BusArrival) : TransitTime()

    data object NoTimeTable : TransitTime()
}
