package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response

import com.deepromeet.atcha.transit.domain.bus.BusArrival
import com.deepromeet.atcha.transit.domain.bus.BusCongestion
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrivals
import com.deepromeet.atcha.transit.domain.bus.BusStatus
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

data class GyeonggiBusArrivalItemResponse(
    @field:JacksonXmlProperty(localName = "busArrivalItem")
    val busArrivalItem: BusArrivalItem
)

data class BusArrivalItem(
    @field:JacksonXmlProperty(localName = "routeDestId")
    val routeDestId: Int,
    @field:JacksonXmlProperty(localName = "routeDestName")
    val routeDestName: String,
    @field:JacksonXmlProperty(localName = "routeId")
    val routeId: Int,
    @field:JacksonXmlProperty(localName = "routeName")
    val routeName: String,
    @field:JacksonXmlProperty(localName = "staOrder")
    val staOrder: Int,
    @field:JacksonXmlProperty(localName = "stationId")
    val stationId: String,
    @field:JacksonXmlProperty(localName = "stationNm1")
    val stationNm1: String,
    @field:JacksonXmlProperty(localName = "stationNm2")
    val stationNm2: String,
    @field:JacksonXmlProperty(localName = "turnSeq")
    val turnSeq: Int,
    @field:JacksonXmlProperty(localName = "predictTimeSec1")
    val predictTimeSec1: Int?,
    @field:JacksonXmlProperty(localName = "predictTimeSec2")
    val predictTimeSec2: Int?,
    @field:JacksonXmlProperty(localName = "crowded1")
    val crowded1: Int,
    @field:JacksonXmlProperty(localName = "crowded2")
    val crowded2: Int,
    @field:JacksonXmlProperty(localName = "remainSeatCnt1")
    val remainSeatCnt1: Int,
    @field:JacksonXmlProperty(localName = "remainSeatCnt2")
    val remainSeatCnt2: Int,
    @field:JacksonXmlProperty(localName = "vehId1")
    val vehId1: Int,
    @field:JacksonXmlProperty(localName = "vehId2")
    val vehId2: Int
) {
    fun toRealTimeArrival(): BusRealTimeArrivals {
        val firstRealTimeArrivalInfo = createRealTimeArrivalInfo(predictTimeSec1, crowded1, remainSeatCnt1, vehId1)
        val secondRealTimeArrivalInfo = createRealTimeArrivalInfo(predictTimeSec2, crowded2, remainSeatCnt2, vehId2)
        return BusRealTimeArrivals(
            listOf(firstRealTimeArrivalInfo, secondRealTimeArrivalInfo)
        )
    }

    private fun createRealTimeArrivalInfo(
        predictTimeSec: Int?,
        crowded: Int,
        remainSeatCnt: Int,
        vehId: Int
    ): BusArrival {
        val busCongestion =
            when (crowded) {
                1 -> BusCongestion.LOW
                2 -> BusCongestion.MEDIUM
                3 -> BusCongestion.HIGH
                4 -> BusCongestion.VERY_HIGH
                else -> BusCongestion.UNKNOWN
            }

        return BusArrival(
            vehicleId = vehId.toString(),
            busStatus = determineBusStatus(predictTimeSec),
            remainingTime = predictTimeSec ?: 0,
            remainingStations = null,
            isLast = null,
            busCongestion = busCongestion,
            remainingSeats = remainSeatCnt
        )
    }

    private fun determineBusStatus(predictTimeSec: Int?): BusStatus {
        return when {
            predictTimeSec == null -> BusStatus.END
            predictTimeSec <= 60 -> BusStatus.SOON
            else -> BusStatus.OPERATING
        }
    }
}
