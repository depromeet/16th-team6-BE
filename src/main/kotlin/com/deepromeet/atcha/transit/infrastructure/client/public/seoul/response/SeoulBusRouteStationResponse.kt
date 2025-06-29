package com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusRouteStation
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationId
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.BusStationNumber
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SeoulBusRouteStationResponse(
    @JacksonXmlProperty(localName = "busRouteId")
    val busRouteId: String,
    @JacksonXmlProperty(localName = "busRouteAbrv")
    val busRouteAbrv: String,
    @JacksonXmlProperty(localName = "seq")
    val seq: String,
    @JacksonXmlProperty(localName = "arsId")
    val arsId: String,
    @JacksonXmlProperty(localName = "stationNm")
    val stationNm: String,
    @JacksonXmlProperty(localName = "station")
    val station: String,
    @JacksonXmlProperty(localName = "gpsX")
    val gpsX: String,
    @JacksonXmlProperty(localName = "gpsY")
    val gpsY: String,
    @JacksonXmlProperty(localName = "transYn")
    val transYn: String
) {
    fun toBusRouteStation(turnPoint: Int?): BusRouteStation =
        BusRouteStation(
            busRoute =
                BusRoute(
                    id = BusRouteId(busRouteId),
                    name = busRouteAbrv,
                    serviceRegion = ServiceRegion.SEOUL
                ),
            order = seq.toInt(),
            busStation =
                BusStation(
                    id = BusStationId(station),
                    busStationNumber = BusStationNumber(arsId),
                    busStationMeta =
                        BusStationMeta(
                            name = stationNm,
                            coordinate =
                                Coordinate(
                                    lat = gpsY.toDouble(),
                                    lon = gpsX.toDouble()
                                )
                        )
                ),
            turnPoint = turnPoint
        )
}
