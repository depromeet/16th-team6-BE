package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class GyeonggiBusRouteListResponse(
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "busRouteList")
    val busRouteList: List<GyeonggiBusRoute>
)

data class GyeonggiBusRoute(
    @field:JacksonXmlProperty(localName = "adminName")
    val adminName: String?,
    @field:JacksonXmlProperty(localName = "districtCd")
    val districtCode: Int,
    @field:JacksonXmlProperty(localName = "endStationId")
    val endStationId: String?,
    @field:JacksonXmlProperty(localName = "endStationName")
    val endStationName: String,
    @field:JacksonXmlProperty(localName = "regionName")
    val regionName: String?,
    @field:JacksonXmlProperty(localName = "routeId")
    val routeId: String,
    @field:JacksonXmlProperty(localName = "routeName")
    val routeName: String,
    @field:JacksonXmlProperty(localName = "routeTypeCd")
    val routeTypeCode: String,
    @field:JacksonXmlProperty(localName = "routeTypeName")
    val routeTypeName: String,
    @field:JacksonXmlProperty(localName = "startStationId")
    val startStationId: String,
    @field:JacksonXmlProperty(localName = "startStationName")
    val startStationName: String
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(routeId),
            name = routeName,
            serviceRegion = ServiceRegion.GYEONGGI
        )
    }
}
