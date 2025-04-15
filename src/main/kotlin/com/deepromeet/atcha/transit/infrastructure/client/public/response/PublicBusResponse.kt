package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusRouteStation
import com.deepromeet.atcha.transit.domain.BusServiceHours
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationId
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.BusStationNumber
import com.deepromeet.atcha.transit.domain.BusStatus
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.RealTimeBusArrival
import com.deepromeet.atcha.transit.domain.ServiceRegion
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
    @JacksonXmlProperty(localName = "stId")
    val stId: String,
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
            id = BusStationId(stId),
            busStationNumber = BusStationNumber(arsId),
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
    @JacksonXmlProperty(localName = "busRouteNm")
    val busRouteNm: String
) {
    fun toBusRoute(): BusRoute =
        BusRoute(
            id = BusRouteId(busRouteId),
            name = busRouteNm,
            serviceRegion = ServiceRegion.SEOUL
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
    fun toBusArrival(): BusArrival {
        val realTimeBusArrivals =
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

        return BusArrival(
            busRoute =
                BusRoute(
                    id = BusRouteId(busRouteId),
                    name = busRouteAbrv,
                    serviceRegion = ServiceRegion.SEOUL
                ),
            busStationId = BusStationId(arsId),
            stationName = stNm,
            busTimeTable =
                BusTimeTable(
                    firstTime = parseDateTime(firstTm),
                    lastTime = parseDateTime(lastTm),
                    term = term.toInt()
                ),
            realTimeInfo = realTimeBusArrivals
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
    ): RealTimeBusArrival {
        val busStatus = determineBusStatus(arrivalMessage)

        val busCongestion =
            when (rerdieDiv) {
                0 -> BusCongestion.UNKNOWN
                2 -> BusCongestion.UNKNOWN
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
                0 -> 0
                2 -> rerideNum
                4 -> 0
                else -> throw IllegalArgumentException("Unknown rerdieDiv: $rerdieDiv")
            }

        return RealTimeBusArrival(
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

    private fun parseDateTime(dateTimeString: String): LocalDateTime? {
        if (dateTimeString.length == 10) {
            return null
        }
        return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER)
    }

    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }
}

data class BusPositionResponse(
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

data class BusRouteInfoResponse(
    @JacksonXmlProperty(localName = "busRouteAbrv")
    val busRouteAbrv: String,
    @JacksonXmlProperty(localName = "busRouteId")
    val busRouteId: String,
    @JacksonXmlProperty(localName = "stStationNm")
    val stStationNm: String,
    @JacksonXmlProperty(localName = "edStationNm")
    val edStationNm: String,
    @JacksonXmlProperty(localName = "term")
    val term: String,
    @JacksonXmlProperty(localName = "firstBusTm")
    val firstBusTm: String,
    @JacksonXmlProperty(localName = "lastBusTm")
    val lastBusTm: String
) {
    fun toBusRouteOperationInfo(): BusRouteOperationInfo {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

        return BusRouteOperationInfo(
            startStationName = stStationNm,
            endStationName = edStationNm,
            serviceHours =
                listOf(
                    BusServiceHours(
                        dailyType = DailyType.WEEKDAY,
                        startTime = LocalDateTime.parse(firstBusTm, formatter),
                        endTime = LocalDateTime.parse(lastBusTm, formatter),
                        term = term.toIntOrNull() ?: 0
                    )
                )
        )
    }
}

data class BusRouteStationResponse(
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
    fun toBusRouteStation(): BusRouteStation =
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
                )
        )
}
