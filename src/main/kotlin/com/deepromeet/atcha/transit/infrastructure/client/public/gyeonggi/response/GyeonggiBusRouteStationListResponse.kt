package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteStation
import com.deepromeet.atcha.transit.domain.bus.BusStation
import com.deepromeet.atcha.transit.domain.bus.BusStationId
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import com.deepromeet.atcha.transit.domain.bus.BusStationNumber
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class GyeonggiBusRouteStationListResponse(
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "busRouteStationList")
    val busRouteStationList: List<GyeonggiBusRouteStation>
)

data class GyeonggiBusRouteStation(
    @field:JacksonXmlProperty(localName = "regionName")
    val regionName: String,
    @field:JacksonXmlProperty(localName = "stationId")
    val stationId: String,
    @field:JacksonXmlProperty(localName = "mobileNo")
    val mobileNo: String = "0",
    @field:JacksonXmlProperty(localName = "stationName")
    val stationName: String,
    @field:JacksonXmlProperty(localName = "x")
    val x: String,
    @field:JacksonXmlProperty(localName = "y")
    val y: String,
    @field:JacksonXmlProperty(localName = "stationSeq")
    val stationSeq: Int,
    @field:JacksonXmlProperty(localName = "turnSeq")
    val turnSeq: Int,
    @field:JacksonXmlProperty(localName = "turnYn")
    val turnYn: String
) {
    fun toBusRouteStation(busRoute: BusRoute): BusRouteStation {
        return BusRouteStation(
            busRoute = busRoute,
            busStation =
                BusStation(
                    id = BusStationId(stationId),
                    busStationNumber = BusStationNumber(mobileNo.trim()),
                    busStationMeta =
                        BusStationMeta(
                            name = stationName,
                            coordinate = Coordinate(y.toDouble(), x.toDouble())
                        )
                ),
            order = stationSeq,
            turnPoint = turnSeq
        )
    }
}
