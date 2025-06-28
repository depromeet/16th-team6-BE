package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusRealTimeArrival
import com.deepromeet.atcha.transit.domain.BusRealTimeInfo
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusRouteStation
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusServiceHours
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationId
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.BusStationNumber
import com.deepromeet.atcha.transit.domain.BusStatus
import com.deepromeet.atcha.transit.domain.BusTimeParser
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.BusTravelTimeCalculator
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDate
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
    val headerCd: Int?,
    @JacksonXmlProperty(localName = "headerMsg")
    val headerMsg: String?,
    @JacksonXmlProperty(localName = "itemCount")
    val itemCount: Int?,
    @JacksonXmlProperty(localName = "numOfRows")
    val numOfRows: Int?,
    @JacksonXmlProperty(localName = "pageNo")
    val pageNo: Int?,
    @JacksonXmlProperty(localName = "resultCode")
    val resultCode: Int?,
    @JacksonXmlProperty(localName = "resultMsg")
    val resultMessage: String?,
    @JacksonXmlProperty(localName = "totalCount")
    val totalCount: Int?
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
    fun toBusSchedule(station: BusStation): BusSchedule =
        BusSchedule(
            busRoute =
                BusRoute(
                    id = BusRouteId(busRouteId),
                    name = busRouteAbrv,
                    serviceRegion = ServiceRegion.SEOUL
                ),
            busStation = station,
            busTimeTable =
                BusTimeTable(
                    firstTime = parseDateTime(firstTm),
                    lastTime = parseDateTime(lastTm),
                    term = term.toInt()
                )
        )

    fun toBusRealTimeArrival(): BusRealTimeArrival {
        return BusRealTimeArrival(
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
    ): BusRealTimeInfo {
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

        return BusRealTimeInfo(
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
                ),
            turnPoint = turnPoint
        )
}

data class SeoulBusRouteInfoResponse(
    @JacksonXmlProperty(localName = "busRouteAbrv")
    val busRouteAbrv: String,
    @JacksonXmlProperty(localName = "busRouteId")
    val busRouteId: String,
    @JacksonXmlProperty(localName = "busRouteNm")
    val busRouteName: String,
    @JacksonXmlProperty(localName = "corpNm")
    val corpName: String,
    @JacksonXmlProperty(localName = "edStationNm")
    val endStationName: String,
    @JacksonXmlProperty(localName = "firstBusTm")
    val firstBusTime: String,
    @JacksonXmlProperty(localName = "firstLowTm")
    val firstLowBusTime: String,
    @JacksonXmlProperty(localName = "lastBusTm")
    val lastBusTime: String,
    @JacksonXmlProperty(localName = "lastBusYn")
    val lastBusYn: String?,
    @JacksonXmlProperty(localName = "lastLowTm")
    val lastLowBusTime: String,
    @JacksonXmlProperty(localName = "length")
    val length: String,
    @JacksonXmlProperty(localName = "routeType")
    val routeType: Int,
    @JacksonXmlProperty(localName = "stStationNm")
    val startStationName: String,
    @JacksonXmlProperty(localName = "term")
    val term: Int
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(busRouteId),
            name = busRouteName,
            serviceRegion = ServiceRegion.SEOUL
        )
    }

    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }
}

data class IncheonBusStationResponse(
    @JacksonXmlProperty(localName = "ADMINNM")
    val adminName: String,
    @JacksonXmlProperty(localName = "BSTOPID")
    val stationId: String,
    @JacksonXmlProperty(localName = "BSTOPNM")
    val stationName: String,
    @JacksonXmlProperty(localName = "CENTERLANEYN")
    val centerLaneYn: String,
    @JacksonXmlProperty(localName = "POSX")
    val positionX: String,
    @JacksonXmlProperty(localName = "POSY")
    val positionY: String,
    @JacksonXmlProperty(localName = "SHORT_BSTOPID")
    val shortStationId: String
) {
    fun toBusStation(transformCoordinate: Coordinate): BusStation {
        return BusStation(
            id = BusStationId(stationId),
            busStationNumber = BusStationNumber(shortStationId),
            busStationMeta =
                BusStationMeta(
                    name = stationName,
                    coordinate = transformCoordinate
                )
        )
    }
}

data class IncheonBusStationRouteResponse(
    @JacksonXmlProperty(localName = "BSTOPID")
    val stationId: String,
    @JacksonXmlProperty(localName = "BSTOPNM")
    val stationName: String,
    @JacksonXmlProperty(localName = "BSTOPSEQ")
    val stationSequence: Int,
    @JacksonXmlProperty(localName = "DESTINATION")
    val destination: String,
    @JacksonXmlProperty(localName = "DEST_BSTOPID")
    val destinationStationId: String,
    @JacksonXmlProperty(localName = "DIRCD")
    val directionCode: Int,
    @JacksonXmlProperty(localName = "PATHSEQ")
    val pathSequence: Int,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String,
    @JacksonXmlProperty(localName = "ROUTENO")
    val routeNumber: String,
    @JacksonXmlProperty(localName = "ROUTETPCD")
    val routeType: Int
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(routeId),
            name = routeNumber,
            serviceRegion = ServiceRegion.INCHEON
        )
    }
}

data class IncheonBusRouteStationListResponse(
    @JacksonXmlProperty(localName = "ADMINNM")
    val adminName: String,
    @JacksonXmlProperty(localName = "BSTOPID")
    val stationId: String,
    @JacksonXmlProperty(localName = "BSTOPNM")
    val stationName: String,
    @JacksonXmlProperty(localName = "BSTOPSEQ")
    val stationSequence: Int,
    @JacksonXmlProperty(localName = "DIRCD")
    val directionCode: Int,
    @JacksonXmlProperty(localName = "PATHSEQ")
    val pathSequence: Int,
    @JacksonXmlProperty(localName = "POSX")
    val positionX: String,
    @JacksonXmlProperty(localName = "POSY")
    val positionY: String,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String,
    @JacksonXmlProperty(localName = "SHORT_BSTOPID")
    val shortStationId: String
) {
    fun toBusRouteStation(
        busRoute: BusRoute,
        turnPoint: Int?,
        transformCoordinate: Coordinate
    ): BusRouteStation {
        val busStation =
            BusStation(
                id = BusStationId(stationId),
                busStationNumber = BusStationNumber(shortStationId),
                busStationMeta =
                    BusStationMeta(
                        name = stationName,
                        coordinate = transformCoordinate
                    )
            )

        return BusRouteStation(
            busRoute = busRoute,
            order = stationSequence,
            busStation = busStation,
            turnPoint = turnPoint
        )
    }
}

data class IncheonBusRouteInfoResponse(
    @JacksonXmlProperty(localName = "ADMINNM")
    val adminName: String,
    @JacksonXmlProperty(localName = "DEST_BSTOPID")
    val destinationStationId: String,
    @JacksonXmlProperty(localName = "DEST_BSTOPNM")
    val destinationStationName: String,
    @JacksonXmlProperty(localName = "FBUS_DEPHMS")
    val firstBusTime: String,
    @JacksonXmlProperty(localName = "LBUS_DEPHMS")
    val lastBusTime: String,
    @JacksonXmlProperty(localName = "MAX_ALLOCGAP")
    val maxDispatchInterval: Int,
    @JacksonXmlProperty(localName = "MIN_ALLOCGAP")
    val minDispatchInterval: Int,
    @JacksonXmlProperty(localName = "ORIGIN_BSTOPID")
    val originStationId: String,
    @JacksonXmlProperty(localName = "ORIGIN_BSTOPNM")
    val originStationName: String,
    @JacksonXmlProperty(localName = "ROUTEID")
    val routeId: String,
    @JacksonXmlProperty(localName = "ROUTELEN")
    val routeLength: String,
    @JacksonXmlProperty(localName = "ROUTENO")
    val routeNumber: String,
    @JacksonXmlProperty(localName = "ROUTETPCD")
    val routeType: String,
    @JacksonXmlProperty(localName = "TURN_BSTOPID")
    val turnStationId: String,
    @JacksonXmlProperty(localName = "TURN_BSTOPNM")
    val turnStationName: String
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(routeId),
            name = routeNumber,
            serviceRegion = ServiceRegion.INCHEON
        )
    }

    fun toBusSchedule(busRouteStation: BusRouteStation): BusSchedule {
        val travelTime = BusTravelTimeCalculator.calculate(busRouteStation, false)

        return BusSchedule(
            busRoute = busRouteStation.busRoute,
            busStation = busRouteStation.busStation,
            busTimeTable =
                BusTimeTable(
                    firstTime =
                        BusTimeParser.parseTime(
                            firstBusTime,
                            LocalDate.now(),
                            TIME_FORMATTER
                        ).plusMinutes(travelTime),
                    lastTime =
                        BusTimeParser.parseTime(
                            lastBusTime,
                            LocalDate.now(),
                            TIME_FORMATTER
                        ).plusMinutes(travelTime),
                    term = maxDispatchInterval
                )
        )
    }

    fun toBusRouteOperationInfo(): BusRouteOperationInfo {
        return BusRouteOperationInfo(
            originStationName,
            destinationStationName,
            serviceHours =
                listOf(
                    BusServiceHours(
                        dailyType = DailyType.WEEKDAY,
                        startTime = BusTimeParser.parseTime(firstBusTime, LocalDate.now(), TIME_FORMATTER),
                        endTime = BusTimeParser.parseTime(lastBusTime, LocalDate.now(), TIME_FORMATTER),
                        term = maxDispatchInterval
                    )
                )
        )
    }

    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm")
    }
}

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
            remainingSeats = remainingSeat
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
            0 -> BusCongestion.UNKNOWN
            1 -> BusCongestion.LOW
            2 -> BusCongestion.MEDIUM
            3 -> BusCongestion.HIGH
            else -> BusCongestion.UNKNOWN
        }
}
