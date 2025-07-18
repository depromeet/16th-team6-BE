package com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response

import com.deepromeet.atcha.transit.domain.bus.BusCongestion
import com.deepromeet.atcha.transit.domain.bus.BusPosition
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class SeoulBusPositionResponse(
    @JacksonXmlProperty(localName = "gpsX")
    val gpsX: String,
    @JacksonXmlProperty(localName = "gpsY")
    val gpsY: String,
    @JacksonXmlProperty(localName = "sectOrd")
    val sectOrd: String,
    @JacksonXmlProperty(localName = "sectDist")
    val sectDist: String,
    @JacksonXmlProperty(localName = "vehId")
    val vehId: String,
    @JacksonXmlProperty(localName = "plainNo")
    val plainNo: String,
    @JacksonXmlProperty(localName = "fullSectDist")
    val fullSectDist: String? = "0",
    @JacksonXmlProperty(localName = "congetion")
    val congetion: String? = "0"
) {
    fun toBusPosition(): BusPosition {
        val busCongestion =
            when (congetion) {
                "0" -> BusCongestion.UNKNOWN
                "3" -> BusCongestion.LOW
                "4" -> BusCongestion.MEDIUM
                "5" -> BusCongestion.HIGH
                "6" -> BusCongestion.VERY_HIGH
                else -> throw IllegalArgumentException("Unknown bus congestion: $congetion")
            }

        return BusPosition(
            vehicleId = vehId,
            sectionOrder = sectOrd.toInt(),
            vehicleNumber = plainNo,
            fullSectionDistance = fullSectDist?.toDouble() ?: 0.0,
            currentSectionDistance = sectDist.toDouble(),
            busCongestion = busCongestion
        )
    }
}
