package com.deepromeet.atcha.transit.infrastructure.cache

import com.deepromeet.atcha.transit.domain.TransitInfo
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
abstract class TransitInfoMixIn
