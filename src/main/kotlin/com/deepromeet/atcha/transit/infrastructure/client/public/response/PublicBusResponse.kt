package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.ArsId
import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.BusStatus
import com.deepromeet.atcha.transit.domain.RealTimeBusArrival
import com.deepromeet.atcha.transit.domain.RouteId
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JacksonXmlRootElement(localName = "ServiceResult")
data class ServiceResult<T>(
    @JacksonXmlProperty(localName = "msgHeader")
    val msgHeader: MsgHeader,
    @JacksonXmlProperty(localName = "msgBody")
    val msgBody: MsgBody<T>
)

data class MsgHeader(
    @JacksonXmlProperty(localName = "headerCd")
    val headerCd: Int,
    @JacksonXmlProperty(localName = "headerMsg")
    val headerMsg: String,
    @JacksonXmlProperty(localName = "itemCount")
    val itemCount: Int
)

data class MsgBody<T>(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "itemList")
    val itemList: List<T>?
)

data class StationResponse(
    @JacksonXmlProperty(localName = "arsId")
    val arsId: String,
    @JacksonXmlProperty(localName = "stNm")
    val stNm: String,
    @JacksonXmlProperty(localName = "tmX")
    val tmX: String,
    @JacksonXmlProperty(localName = "tmY")
    val tmY: String
) {
    fun toBusStation(): BusStation =
        BusStation(
            arsId = ArsId(arsId),
            busStationMeta =
                BusStationMeta(
                    name = stNm,
                    coordinate =
                        Coordinate(
                            lat = tmY.toDouble(),
                            lon = tmX.toDouble()
                        )
                )
        )
}

data class BusRouteResponse(
    @JacksonXmlProperty(localName = "busRouteId")
    val busRouteId: String,
    @JacksonXmlProperty(localName = "busRouteAbrv")
    val busRouteAbrv: String
) {
    fun toBusRoute(): BusRoute =
        BusRoute(
            id = RouteId(busRouteId),
            name = busRouteAbrv
        )
}

data class BusArrivalResponse(
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
    val term: String
) {
    fun toBusArrival(): BusArrival {
        val realTimeBusArrivals =
            listOf(
                createArrivalInfo(
                    arrivalMessage = arrmsg1,
                    sectionOrder = sectOrd1,
                    isLast = isLast1
                ),
                createArrivalInfo(
                    arrivalMessage = arrmsg2,
                    sectionOrder = sectOrd2,
                    isLast = isLast2
                )
            )

        return BusArrival(
            routeId = RouteId(busRouteId),
            routeName = busRouteAbrv,
            arsId = ArsId(arsId),
            stationName = stNm,
            lastTime = parseDateTime(lastTm),
            term = term.toInt(),
            realTimeInfo = realTimeBusArrivals
        )
    }

    private fun createArrivalInfo(
        arrivalMessage: String,
        sectionOrder: String,
        isLast: String
    ): RealTimeBusArrival {
        val busStatus = determineBusStatus(arrivalMessage)
        val remainingTime = calculateRemainingTime(arrivalMessage, busStatus)

        return RealTimeBusArrival(
            busStatus = busStatus,
            remainingTime = remainingTime,
            remainingStations = staOrd.toInt() - sectionOrder.toInt(),
            isLast = isLast == "1"
        )
    }

    private fun calculateRemainingTime(
        arrivalMessage: String,
        busStatus: BusStatus
    ): Int = if (busStatus == BusStatus.OPERATING) extractTimeFromMessage(arrivalMessage) else 0

    private fun determineBusStatus(arrivalMessage: String): BusStatus =
        when (arrivalMessage) {
            "출발대기" -> BusStatus.WAITING
            "곧 도착" -> BusStatus.SOON
            "운행종료" -> BusStatus.END
            else -> BusStatus.OPERATING
        }

    private fun parseDateTime(dateTimeString: String): LocalDateTime =
        LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER)

    private fun extractTimeFromMessage(timeString: String): Int {
        val matchResult = TIME_PATTERN.find(timeString) ?: return 0

        val (minutes, seconds) = matchResult.destructured
        return minutes.toInt() * 60 + seconds.toInt()
    }

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        private val TIME_PATTERN = """(\d+)분(\d+)초후""".toRegex()
    }
}
