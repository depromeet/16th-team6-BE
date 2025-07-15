package com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response

import com.deepromeet.atcha.transit.domain.bus.BusCongestion
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class IncheonBusPositionResponse(
    @JacksonXmlProperty(localName = "BUSID")
    val busId: String,
    @JacksonXmlProperty(localName = "BUS_NUM_PLATE")
    val busNumberPlate: String,
    @JacksonXmlProperty(localName = "CONGESTION")
    val congestion: Int,
    @JacksonXmlProperty(localName = "DIRCD")
    val directionCode: Int,
    @JacksonXmlProperty(localName = "LASTBUSYN")
    val lastBusYn: Int,
    @JacksonXmlProperty(localName = "LATEST_STOPSEQ")
    val latestStopSeq: Int,
    @JacksonXmlProperty(localName = "LATEST_STOP_ID")
    val latestStopId: String,
    @JacksonXmlProperty(localName = "LATEST_STOP_NAME")
    val latestStopName: String,
    @JacksonXmlProperty(localName = "LOW_TP_CD")
    val lowTypeCode: Int,
    @JacksonXmlProperty(localName = "PATHSEQ")
    val pathSeq: Int,
    @JacksonXmlProperty(localName = "REMAIND_SEAT")
    val remainingSeat: Int,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String
) {
    fun toBusPosition(): BusPosition {
        val busCongestion =
            when (congestion) {
                1 -> BusCongestion.LOW
                2 -> BusCongestion.MEDIUM
                3 -> BusCongestion.HIGH
                255 -> BusCongestion.UNKNOWN
                else -> BusCongestion.UNKNOWN
            }

        return BusPosition(
            vehicleId = busId,
            sectionOrder = latestStopSeq,
            vehicleNumber = busNumberPlate,
            fullSectionDistance = 0.0,
            currentSectionDistance = 0.0,
            busCongestion = busCongestion,
            remainSeats = if (remainingSeat == 255) null else remainingSeat
        )
    }
}
