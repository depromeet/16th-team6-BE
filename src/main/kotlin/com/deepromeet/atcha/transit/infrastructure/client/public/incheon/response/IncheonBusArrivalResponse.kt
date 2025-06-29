package com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response

import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.BusRealTimeInfo
import com.deepromeet.atcha.transit.domain.BusStatus
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class IncheonBusArrivalResponse(
    @JacksonXmlProperty(localName = "ARRIVALESTIMATETIME")
    val arrivalEstimateTime: String,
    @JacksonXmlProperty(localName = "BSTOPID")
    val stationId: String,
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
    @JacksonXmlProperty(localName = "LATEST_STOP_ID")
    val latestStopId: String,
    @JacksonXmlProperty(localName = "LATEST_STOP_NAME")
    val latestStopName: String,
    @JacksonXmlProperty(localName = "LOW_TP_CD")
    val lowTypeCode: Int,
    @JacksonXmlProperty(localName = "REMAIND_SEAT")
    val remainingSeat: Int,
    @JacksonXmlProperty(localName = "REST_STOP_COUNT")
    val restStopCount: Int,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String
) {
    fun toBusRealTimeArrival(): BusRealTimeArrival = BusRealTimeArrival(listOf(toBusRealTimeInfo()))

    private fun toBusRealTimeInfo(): BusRealTimeInfo {
        return BusRealTimeInfo(
            vehicleId = busId,
            busStatus = determineBusStatus(arrivalEstimateTime.toInt(), restStopCount),
            remainingTime = arrivalEstimateTime.toInt(),
            remainingStations = restStopCount,
            isLast = lastBusYn == 1,
            busCongestion = toBusCongestion(congestion),
            remainingSeats = if (remainingSeat == 255) null else remainingSeat
        )
    }

    private fun determineBusStatus(
        remainSec: Int,
        remainStops: Int
    ): BusStatus =
        when {
            remainStops <= 1 -> BusStatus.SOON
            remainSec <= 60 -> BusStatus.SOON
            else -> BusStatus.OPERATING
        }

    private fun toBusCongestion(code: Int): BusCongestion =
        when (code) {
            1 -> BusCongestion.LOW
            2 -> BusCongestion.MEDIUM
            3 -> BusCongestion.HIGH
            255 -> BusCongestion.UNKNOWN
            else -> BusCongestion.UNKNOWN
        }
}
