package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response

import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusPosition
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class GyeonggiBusLocationListResponse(
    @field:JacksonXmlElementWrapper(useWrapping = false)
    @field:JacksonXmlProperty(localName = "busLocationList")
    val busLocationList: List<BusLocationResponse>
)

data class BusLocationResponse(
    @field:JacksonXmlProperty(localName = "crowded")
    val crowded: Int,
    @field:JacksonXmlProperty(localName = "plateNo")
    val plateNo: String,
    @field:JacksonXmlProperty(localName = "remainSeatCnt")
    val remainSeatCnt: Int,
    @field:JacksonXmlProperty(localName = "routeId")
    val routeId: Int,
    @field:JacksonXmlProperty(localName = "stationId")
    val stationId: Int,
    @field:JacksonXmlProperty(localName = "stationSeq")
    val stationSeq: Int,
    @field:JacksonXmlProperty(localName = "vehId")
    val vehId: Int
) {
    fun toBusPosition(): BusPosition {
        val busCongestion =
            when (crowded) {
                1 -> BusCongestion.LOW
                2 -> BusCongestion.MEDIUM
                3 -> BusCongestion.HIGH
                4 -> BusCongestion.VERY_HIGH
                else -> BusCongestion.UNKNOWN
            }

        return BusPosition(
            vehicleId = vehId.toString(),
            sectionOrder = stationSeq,
            vehicleNumber = plateNo,
            fullSectionDistance = null,
            currentSectionDistance = null,
            busCongestion = busCongestion,
            remainSeats = remainSeatCnt
        )
    }
}
