package com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.format.DateTimeFormatter

data class SeoulBusRouteInfoResponse(
    @JacksonXmlProperty(localName = "busRouteAbrv")
    val busRouteAbrv: String,
    @JacksonXmlProperty(localName = "busRouteId")
    val busRouteId: String,
    @JacksonXmlProperty(localName = "busRouteNm")
    val busRouteName: String,
    @JacksonXmlProperty(localName = "corpNm")
    val corpName: String,
    @JacksonXmlProperty(localName = "edStationNm")
    val endStationName: String,
    @JacksonXmlProperty(localName = "firstBusTm")
    val firstBusTime: String,
    @JacksonXmlProperty(localName = "firstLowTm")
    val firstLowBusTime: String,
    @JacksonXmlProperty(localName = "lastBusTm")
    val lastBusTime: String,
    @JacksonXmlProperty(localName = "lastBusYn")
    val lastBusYn: String?,
    @JacksonXmlProperty(localName = "lastLowTm")
    val lastLowBusTime: String,
    @JacksonXmlProperty(localName = "length")
    val length: String,
    @JacksonXmlProperty(localName = "routeType")
    val routeType: Int,
    @JacksonXmlProperty(localName = "stStationNm")
    val startStationName: String,
    @JacksonXmlProperty(localName = "term")
    val term: Int
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(busRouteId),
            name = busRouteName,
            serviceRegion = ServiceRegion.SEOUL
        )
    }

    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }
}
