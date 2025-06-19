package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusDirection
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
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

val log = KotlinLogging.logger {}
const val AVERAGE_MINUTE_PER_STATION = 2

@JacksonXmlRootElement(localName = "response")
data class PublicGyeonggiResponse<T>(
    @field:JacksonXmlProperty(localName = "msgHeader")
    val msgHeader: GyeonggiMsgHeader = GyeonggiMsgHeader(),
    @field:JacksonXmlProperty(localName = "msgBody")
    val msgBody: T?
) {
    data class BusRouteListResponse(
        @field:JacksonXmlElementWrapper(useWrapping = false)
        @field:JacksonXmlProperty(localName = "busRouteList")
        val busRouteList: List<GyeonggiBusRoute>
    )

    data class BusStationResponse(
        @field:JacksonXmlElementWrapper(useWrapping = false)
        @field:JacksonXmlProperty(localName = "busStationList")
        val busStationList: List<GyeonggiBusStation>
    )

    data class BusRouteStationListResponse(
        @field:JacksonXmlElementWrapper(useWrapping = false)
        @field:JacksonXmlProperty(localName = "busRouteStationList")
        val busRouteStationList: List<GyeonggiBusRouteStation>
    ) {
        fun getStation(stationId: BusStationId): GyeonggiBusRouteStation {
            val station = busRouteStationList.find { it.stationId == stationId.value }
            requireNotNull(station) { "Station not found: $stationId" }
            return station
        }

        fun getStation(
            stationName: String,
            staOrder: Int
        ): GyeonggiBusRouteStation {
            val newStationName = stationName.replace("(미정차)", "")

            val station =
                busRouteStationList
                    .filter { it.stationName.contains(newStationName) }
                    .minByOrNull { abs(staOrder - it.stationSeq) }

            requireNotNull(station) { "Station not found: $newStationName" }
            return station
        }
    }

    data class BusRouteInfoResponse(
        @field:JacksonXmlProperty(localName = "busRouteInfoItem")
        val busRouteInfoItem: BusRouteInfoItem
    )

    data class BusRealTimeInfoResponse(
        @field:JacksonXmlProperty(localName = "busArrivalItem")
        val busRealTimeInfoItem: BusRealTimeInfoItem
    )

    data class BusLocationListResponse(
        @field:JacksonXmlElementWrapper(useWrapping = false)
        @field:JacksonXmlProperty(localName = "busLocationList")
        val busLocationList: List<BusLocationResponse>
    )
}

data class GyeonggiMsgHeader(
    @field:JacksonXmlProperty(localName = "queryTime")
    val queryTime: String = "",
    @field:JacksonXmlProperty(localName = "resultCode")
    val resultCode: String = "",
    @field:JacksonXmlProperty(localName = "resultMessage")
    val resultMessage: String = ""
) {
    fun isEmptyResponse(): Boolean {
        return resultCode == "4"
    }
}

data class BusRealTimeInfoItem(
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
    fun toRealTimeArrival(): BusRealTimeArrival {
        val firstRealTimeArrivalInfo = createRealTimeArrivalInfo(predictTimeSec1, crowded1, remainSeatCnt1, vehId1)
        val secondRealTimeArrivalInfo = createRealTimeArrivalInfo(predictTimeSec2, crowded2, remainSeatCnt2, vehId2)
        return BusRealTimeArrival(
            listOf(firstRealTimeArrivalInfo, secondRealTimeArrivalInfo)
        )
    }

    private fun createRealTimeArrivalInfo(
        predictTimeSec: Int?,
        crowded: Int,
        remainSeatCnt: Int,
        vehId: Int
    ): BusRealTimeInfo {
        val busCongestion =
            when (crowded) {
                0 -> BusCongestion.UNKNOWN
                1 -> BusCongestion.LOW
                2 -> BusCongestion.MEDIUM
                3 -> BusCongestion.HIGH
                4 -> BusCongestion.VERY_HIGH
                else -> throw IllegalArgumentException("Unknown bus congestion: $crowded")
            }

        return BusRealTimeInfo(
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
    fun toBusStation(): BusStation =
        BusStation(
            id = BusStationId(stationId),
            busStationNumber = BusStationNumber(mobileNo.trim()),
            busStationMeta =
                BusStationMeta(
                    name = stationName,
                    coordinate = Coordinate(y.toDouble(), x.toDouble())
                )
        )

    fun getDirection(): BusDirection {
        // 경기도는 기점 -> 종점은 상행, 종점 -> 기점은 하행
        return if (stationSeq < turnSeq) BusDirection.UP else BusDirection.DOWN
    }

    fun getStationsCountFromStart(): Int {
        return if (stationSeq < turnSeq) {
            stationSeq
        } else {
            stationSeq - turnSeq
        }
    }

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
            order = stationSeq
        )
    }
}

data class GyeonggiBusStation(
    @field:JacksonXmlProperty(localName = "centerYn")
    val centerYn: String?,
    @field:JacksonXmlProperty(localName = "mobileNo")
    val mobileNo: String,
    @field:JacksonXmlProperty(localName = "regionName")
    val regionName: String?,
    @field:JacksonXmlProperty(localName = "stationId")
    val stationId: String,
    @field:JacksonXmlProperty(localName = "stationName")
    val stationName: String,
    @field:JacksonXmlProperty(localName = "x")
    val x: String,
    @field:JacksonXmlProperty(localName = "y")
    val y: String
) {
    fun toBusStation(): BusStation {
        return BusStation(
            id = BusStationId(stationId),
            busStationNumber = BusStationNumber(mobileNo),
            busStationMeta =
                BusStationMeta(
                    name = stationName,
                    coordinate = Coordinate(y.toDouble(), x.toDouble())
                )
        )
    }
}

data class GyeonggiBusRoute(
    @field:JacksonXmlProperty(localName = "routeId")
    val routeId: String,
    @field:JacksonXmlProperty(localName = "routeName")
    val routeName: String
) {
    fun toBusRoute(): BusRoute {
        return BusRoute(
            id = BusRouteId(routeId),
            name = routeName,
            serviceRegion = ServiceRegion.GYEONGGI
        )
    }
}

data class BusRouteInfoItem(
    @field:JacksonXmlProperty(localName = "routeId")
    val routeId: String,
    @field:JacksonXmlProperty(localName = "routeName")
    val routeName: String,
    @field:JacksonXmlProperty(localName = "startStationId")
    val startStationId: String,
    @field:JacksonXmlProperty(localName = "startStationName")
    val startStationName: String,
    @field:JacksonXmlProperty(localName = "endStationId")
    val endStationId: String,
    @field:JacksonXmlProperty(localName = "endStationName")
    val endStationName: String,
    @field:JacksonXmlProperty(localName = "turnStID")
    val turnStID: String,
    @field:JacksonXmlProperty(localName = "turnStNm")
    val turnStNm: String,
    @field:JacksonXmlProperty(localName = "upFirstTime")
    val upFirstTime: String?,
    @field:JacksonXmlProperty(localName = "downFirstTime")
    val downFirstTime: String?,
    @field:JacksonXmlProperty(localName = "satUpFirstTime")
    val satUpFirstTime: String?,
    @field:JacksonXmlProperty(localName = "satDownFirstTime")
    val satDownFirstTime: String?,
    @field:JacksonXmlProperty(localName = "sunUpFirstTime")
    val sunUpFirstTime: String?,
    @field:JacksonXmlProperty(localName = "sunDownFirstTime")
    val sunDownFirstTime: String?,
    @field:JacksonXmlProperty(localName = "weUpFirstTime")
    val weUpFirstTime: String?,
    @field:JacksonXmlProperty(localName = "weDownFirstTime")
    val weDownFirstTime: String?,
    @field:JacksonXmlProperty(localName = "upLastTime")
    val upLastTime: String?,
    @field:JacksonXmlProperty(localName = "downLastTime")
    val downLastTime: String?,
    @field:JacksonXmlProperty(localName = "satUpLastTime")
    val satUpLastTime: String?,
    @field:JacksonXmlProperty(localName = "satDownLastTime")
    val satDownLastTime: String?,
    @field:JacksonXmlProperty(localName = "sunUpLastTime")
    val sunUpLastTime: String?,
    @field:JacksonXmlProperty(localName = "sunDownLastTime")
    val sunDownLastTime: String?,
    @field:JacksonXmlProperty(localName = "weUpLastTime")
    val weUpLastTime: String?,
    @field:JacksonXmlProperty(localName = "weDownLastTime")
    val weDownLastTime: String?,
    @field:JacksonXmlProperty(localName = "peekAlloc")
    val peekAlloc: Int,
    @field:JacksonXmlProperty(localName = "nPeekAlloc")
    val nPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "wePeekAlloc")
    val wePeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "weNPeekAlloc")
    val weNPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "satPeekAlloc")
    val satPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "satNPeekAlloc")
    val satNPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "sunPeekAlloc")
    val sunPeekAlloc: Int,
    @field:JacksonXmlProperty(localName = "sunNPeekAlloc")
    val sunNPeekAlloc: Int
) {
    enum class BusTimeType {
        FIRST,
        LAST
    }

    fun toBusSchedule(
        dailyType: DailyType,
        stationInfo: GyeonggiBusRouteStation
    ): BusSchedule? {
        val firstTime = getBusTime(dailyType, stationInfo.getDirection(), BusTimeType.FIRST) ?: return null
        val lastTime = getBusTime(dailyType, stationInfo.getDirection(), BusTimeType.LAST) ?: return null
        val term = getTerm(dailyType)

        val stationsCountFromStart = stationInfo.getStationsCountFromStart()

        val travelTimeFromStart = (stationsCountFromStart * AVERAGE_MINUTE_PER_STATION).toLong()

        return BusSchedule(
            busRoute =
                BusRoute(
                    id = BusRouteId(routeId),
                    name = routeName,
                    serviceRegion = ServiceRegion.GYEONGGI
                ),
            busStation = stationInfo.toBusStation(),
            busTimeTable =
                BusTimeTable(
                    firstTime = firstTime.plusSeconds(travelTimeFromStart),
                    lastTime = lastTime.plusSeconds(travelTimeFromStart),
                    term = term
                )
        )
    }

    fun toBusRouteOperationInfo(): BusRouteOperationInfo {
        return BusRouteOperationInfo(
            startStationName = startStationName,
            endStationName = endStationName,
            serviceHours =
                listOfNotNull(
                    createBusServiceHours(DailyType.WEEKDAY, BusDirection.UP),
                    createBusServiceHours(DailyType.WEEKDAY, BusDirection.DOWN),
                    createBusServiceHours(DailyType.SATURDAY, BusDirection.UP),
                    createBusServiceHours(DailyType.SATURDAY, BusDirection.DOWN),
                    createBusServiceHours(DailyType.HOLIDAY, BusDirection.UP),
                    createBusServiceHours(DailyType.HOLIDAY, BusDirection.DOWN)
                )
        )
    }

    private fun getBusTime(
        dailyType: DailyType,
        busDirection: BusDirection,
        timeType: BusTimeType
    ): LocalDateTime? {
        val timeStr: String? = getTimeString(dailyType, busDirection, timeType)
        return parseTime(timeStr)
    }

    private fun getTimeString(
        dailyType: DailyType,
        busDirection: BusDirection,
        timeType: BusTimeType
    ): String? {
        return when (dailyType) {
            DailyType.WEEKDAY -> {
                when (busDirection) {
                    BusDirection.UP -> if (timeType == BusTimeType.FIRST) upFirstTime else upLastTime
                    BusDirection.DOWN -> if (timeType == BusTimeType.FIRST) downFirstTime else downLastTime
                }
            }
            DailyType.SATURDAY -> {
                when (busDirection) {
                    BusDirection.UP -> if (timeType == BusTimeType.FIRST) satUpFirstTime else satUpLastTime
                    BusDirection.DOWN -> if (timeType == BusTimeType.FIRST) satDownFirstTime else satDownLastTime
                }
            }
            DailyType.SUNDAY -> {
                when (busDirection) {
                    BusDirection.UP -> if (timeType == BusTimeType.FIRST) sunUpFirstTime else sunUpLastTime
                    BusDirection.DOWN -> if (timeType == BusTimeType.FIRST) sunDownFirstTime else sunDownLastTime
                }
            }
            DailyType.HOLIDAY -> {
                when (busDirection) {
                    BusDirection.UP -> if (timeType == BusTimeType.FIRST) weUpFirstTime else weUpLastTime
                    BusDirection.DOWN -> if (timeType == BusTimeType.FIRST) weDownFirstTime else weDownLastTime
                }
            }
        }
    }

    private fun parseTime(timeStr: String?): LocalDateTime? {
        if (timeStr == "0" || timeStr == null) {
            log.warn { "노선 이름: ${this.routeName}, 노선 번호: ${this.routeName} - 시간 값이 '0' 입니다." }
            return null
        }

        try {
            val localTime = LocalTime.parse(timeStr)
            val date =
                if (localTime.isBefore(LocalTime.of(3, 0))) {
                    LocalDate.now().plusDays(1)
                } else {
                    LocalDate.now()
                }
            return LocalDateTime.of(date, localTime)
        } catch (e: Exception) {
            log.warn { "노선 이름: ${this.routeName}, 노선 번호: ${this.routeName} - 시간 파싱 오류: $timeStr" }
            return null
        }
    }

    private fun createBusServiceHours(
        dailyType: DailyType,
        busDirection: BusDirection
    ): BusServiceHours? {
        val firstTimeStr = getTimeString(dailyType, busDirection, BusTimeType.FIRST)
        val lastTimeStr = getTimeString(dailyType, busDirection, BusTimeType.LAST)

        if (firstTimeStr.isNullOrBlank() || lastTimeStr.isNullOrBlank()) return null

        val term =
            when (dailyType) {
                DailyType.WEEKDAY -> peekAlloc
                DailyType.SATURDAY -> satPeekAlloc
                DailyType.SUNDAY -> sunPeekAlloc
                DailyType.HOLIDAY -> wePeekAlloc
            }

        return BusServiceHours(
            dailyType = dailyType,
            busDirection = busDirection,
            startTime = parseTime(firstTimeStr),
            endTime = parseTime(lastTimeStr),
            term = term
        )
    }

    private fun getTerm(dailyType: DailyType): Int {
        return when (dailyType) {
            DailyType.WEEKDAY -> peekAlloc
            DailyType.SATURDAY -> satPeekAlloc
            DailyType.HOLIDAY -> wePeekAlloc
            DailyType.SUNDAY -> sunPeekAlloc
        }
    }
}

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
                else -> throw IllegalArgumentException(
                    "Unknown bus congestion: $crowded"
                )
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
