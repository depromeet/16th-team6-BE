package com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteStation
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationId
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.BusStationNumber
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class IncheonBusRouteStationListResponse(
    @JacksonXmlProperty(localName = "ADMINNM")
    val adminName: String,
    @JacksonXmlProperty(localName = "BSTOPID")
    val stationId: String,
    @JacksonXmlProperty(localName = "BSTOPNM")
    val stationName: String,
    @JacksonXmlProperty(localName = "BSTOPSEQ")
    val stationSequence: Int,
    @JacksonXmlProperty(localName = "DIRCD")
    val directionCode: Int,
    @JacksonXmlProperty(localName = "PATHSEQ")
    val pathSequence: Int,
    @JacksonXmlProperty(localName = "POSX")
    val positionX: String,
    @JacksonXmlProperty(localName = "POSY")
    val positionY: String,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String,
    @JacksonXmlProperty(localName = "SHORT_BSTOPID")
    val shortStationId: String
) {
    fun toBusRouteStation(
        busRoute: BusRoute,
        turnPoint: Int?,
        transformCoordinate: Coordinate
    ): BusRouteStation {
        val busStation =
            BusStation(
                id = BusStationId(stationId),
                busStationNumber = BusStationNumber(shortStationId),
                busStationMeta =
                    BusStationMeta(
                        name = stationName,
                        coordinate = transformCoordinate
                    )
            )

        return BusRouteStation(
            busRoute = busRoute,
            order = stationSequence,
            busStation = busStation,
            turnPoint = turnPoint
        )
    }
}
