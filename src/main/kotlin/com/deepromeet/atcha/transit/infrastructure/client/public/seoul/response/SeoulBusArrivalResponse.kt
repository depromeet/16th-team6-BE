package com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response

import com.deepromeet.atcha.transit.domain.bus.BusArrival
import com.deepromeet.atcha.transit.domain.bus.BusCongestion
import com.deepromeet.atcha.transit.domain.bus.BusRealTimeArrivals
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusStatus
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SeoulBusArrivalResponse(
    @JacksonXmlProperty(localName = "arsId")
    val arsId: String,
    @JacksonXmlProperty(localName = "arrmsg1")
    val arrmsg1: String,
    @JacksonXmlProperty(localName = "arrmsg2")
    val arrmsg2: String,
    @JacksonXmlProperty(localName = "busRouteId")
    val busRouteId: String,
    @JacksonXmlProperty(localName = "busRouteAbrv")
    val busRouteAbrv: String,
    @JacksonXmlProperty(localName = "firstTm")
    val firstTm: String,
    @JacksonXmlProperty(localName = "lastTm")
    val lastTm: String,
    @JacksonXmlProperty(localName = "stId")
    val stId: String,
    @JacksonXmlProperty(localName = "stNm")
    val stNm: String,
    @JacksonXmlProperty(localName = "sectOrd1")
    val sectOrd1: String,
    @JacksonXmlProperty(localName = "sectOrd2")
    val sectOrd2: String,
    @JacksonXmlProperty(localName = "staOrd")
    val staOrd: String,
    @JacksonXmlProperty(localName = "isLast1")
    val isLast1: String,
    @JacksonXmlProperty(localName = "isLast2")
    val isLast2: String,
    @JacksonXmlProperty(localName = "term")
    val term: String,
    @JacksonXmlProperty(localName = "traTime1")
    val traTime1: String,
    @JacksonXmlProperty(localName = "traTime2")
    val traTime2: String,
    @JacksonXmlProperty(localName = "rerdie_Div1")
    val rerdieDiv1: Int,
    @JacksonXmlProperty(localName = "rerdie_Div2")
    val rerdieDiv2: Int,
    @JacksonXmlProperty(localName = "reride_Num1")
    val rerideNum1: Int,
    @JacksonXmlProperty(localName = "reride_Num2")
    val rerideNum2: Int,
    @JacksonXmlProperty(localName = "vehId1")
    val vehId1: String,
    @JacksonXmlProperty(localName = "vehId2")
    val vehId2: String
) {
    fun toBusSchedule(busRouteInfo: BusRouteInfo): BusSchedule =
        BusSchedule(
            busRouteInfo = busRouteInfo,
            busTimeTable =
                BusTimeTable(
                    firstTime = parseDateTime(firstTm),
                    lastTime = parseDateTime(lastTm),
                    term = term.toInt()
                )
        )

    fun toBusRealTimeArrival(): BusRealTimeArrivals {
        return BusRealTimeArrivals(
            listOf(
                createRealTimeArrivalInfo(
                    arrivalMessage = arrmsg1,
                    sectionOrder = sectOrd1,
                    isLast = isLast1,
                    remainingTime = traTime1,
                    rerdieDiv = rerdieDiv1,
                    rerideNum = rerideNum1,
                    vehId = vehId1
                ),
                createRealTimeArrivalInfo(
                    arrivalMessage = arrmsg2,
                    sectionOrder = sectOrd2,
                    isLast = isLast2,
                    remainingTime = traTime2,
                    rerdieDiv = rerdieDiv2,
                    rerideNum = rerideNum2,
                    vehId = vehId2
                )
            )
        )
    }

    private fun createRealTimeArrivalInfo(
        arrivalMessage: String,
        sectionOrder: String,
        isLast: String,
        remainingTime: String,
        rerdieDiv: Int,
        rerideNum: Int,
        vehId: String
    ): BusArrival {
        val busStatus = determineBusStatus(arrivalMessage)

        val busCongestion =
            when (rerdieDiv) {
                0, 1, 2 -> BusCongestion.UNKNOWN
                4 ->
                    when (rerideNum) {
                        0 -> BusCongestion.UNKNOWN
                        3 -> BusCongestion.LOW
                        4 -> BusCongestion.MEDIUM
                        5 -> BusCongestion.HIGH
                        else -> throw IllegalArgumentException("Unknown bus rerideNum: $rerideNum")
                    }
                else -> throw IllegalArgumentException("Unknown rerdieDiv: $rerdieDiv")
            }

        val remainingSeats =
            when (rerdieDiv) {
                0, 4 -> 0
                1, 2 -> rerideNum
                else -> throw IllegalArgumentException("Unknown rerdieDiv: $rerdieDiv")
            }

        return BusArrival(
            vehicleId = vehId,
            busStatus = busStatus,
            remainingTime = remainingTime.toInt(),
            remainingStations = staOrd.toInt() - sectionOrder.toInt(),
            isLast = isLast == "1",
            busCongestion = busCongestion,
            remainingSeats = remainingSeats
        )
    }

    private fun determineBusStatus(arrivalMessage: String): BusStatus =
        when (arrivalMessage) {
            "출발대기" -> BusStatus.WAITING
            "곧 도착" -> BusStatus.SOON
            "운행종료" -> BusStatus.END
            else -> BusStatus.OPERATING
        }

    private fun parseDateTime(dateTimeString: String): LocalDateTime {
        if (dateTimeString.length == 10) {
            throw TransitException(
                TransitError.INVALID_TIME_FORMAT,
                "Invalid date time format: $dateTimeString. Expected format is 'yyyyMMddHHmmss'."
            )
        }
        return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER)
    }

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }
}
