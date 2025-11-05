package com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.application.bus.BusRouteInfoClient.Companion.NON_STOP_STATION_NAME
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.domain.bus.BusRouteStation
import com.deepromeet.atcha.transit.domain.bus.BusStation
import com.deepromeet.atcha.transit.domain.bus.BusStationId
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.bus.BusStationNumber
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
                    busStationNumber = BusStationNumber(resolveBusStationNumber()),
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

    fun resolveBusStationNumber(): String {
        val trimmedArsId = arsId.trim()
        val isNonStopStation = NON_STOP_STATION_NAME.any { keyword -> stationNm.contains(keyword) }
        return if ((trimmedArsId.isBlank() || trimmedArsId == "0") && isNonStopStation) "미정차" else trimmedArsId
    }
}
