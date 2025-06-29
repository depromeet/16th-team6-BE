package com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class IncheonBusStationRouteResponse(
    @JacksonXmlProperty(localName = "BSTOPID")
    val stationId: String,
    @JacksonXmlProperty(localName = "BSTOPNM")
    val stationName: String,
    @JacksonXmlProperty(localName = "BSTOPSEQ")
    val stationSequence: Int,
    @JacksonXmlProperty(localName = "DESTINATION")
    val destination: String,
    @JacksonXmlProperty(localName = "DEST_BSTOPID")
    val destinationStationId: String,
    @JacksonXmlProperty(localName = "DIRCD")
    val directionCode: Int,
    @JacksonXmlProperty(localName = "PATHSEQ")
    val pathSequence: Int,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String,
    @JacksonXmlProperty(localName = "ROUTENO")
    val routeNumber: String,
    @JacksonXmlProperty(localName = "ROUTETPCD")
    val routeType: Int
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(routeId),
            name = routeNumber,
            serviceRegion = ServiceRegion.INCHEON
        )
    }
}
