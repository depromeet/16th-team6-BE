package com.deepromeet.atcha.transit.infrastructure.client.public.response

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusDirection
import com.deepromeet.atcha.transit.domain.BusPosition
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.BusRouteStation
import com.deepromeet.atcha.transit.domain.BusServiceHours
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusStationId
import com.deepromeet.atcha.transit.domain.BusStationMeta
import com.deepromeet.atcha.transit.domain.BusStatus
import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.RealTimeBusArrival
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.infrastructure.client.public.config.BusStationListDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

data class PublicGyeonggiApiResponse<T>(
    val response: PublicGyeonggiResponse<T>
)

data class GyeonggiMsgHeader(
    val queryTime: String = "",
    val resultCode: String = "00",
    val resultMessage: String = ""
)

data class PublicGyeonggiResponse<T>(
    val msgHeader: GyeonggiMsgHeader = GyeonggiMsgHeader(),
    val msgBody: T
) {
    data class BusRouteListResponse(
        val busRouteList: List<GyeonggiBusRoute>
    )

    data class BusStationResponse(
        @JsonDeserialize(using = BusStationListDeserializer::class)
        val busStationList: List<GyeonggiBusStation>
    )

    data class BusRouteStationListResponse(
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
        val busRouteInfoItem: BusRouteInfoItem
    )

    data class BusArrivalInfoResponse(
        val busArrivalItem: BusArrivalItem
    )

    data class BusLocationListResponse(
        val busLocationList: List<BusLocationResponse>
    )
}

data class BusArrivalItem(
    val routeDestId: Int,
    val routeDestName: String,
    val routeId: Int,
    val routeName: String,
    val staOrder: Int,
    val stationId: String,
    val stationNm1: String,
    val stationNm2: String,
    val turnSeq: Int,
    val predictTimeSec1: Int?,
    val predictTimeSec2: Int?,
    val crowded1: Int,
    val crowded2: Int,
    val remainSeatCnt1: Int,
    val remainSeatCnt2: Int,
    val vehId1: Int,
    val vehId2: Int
) {
    fun toRealTimeBussArrivals(): List<RealTimeBusArrival> {
        val firstRealTimeArrivalInfo = createRealTimeArrivalInfo(predictTimeSec1, crowded1, remainSeatCnt1, vehId1)
        val secondRealTimeArrivalInfo = createRealTimeArrivalInfo(predictTimeSec2, crowded2, remainSeatCnt2, vehId2)
        return listOf(firstRealTimeArrivalInfo, secondRealTimeArrivalInfo)
    }

    fun calculateTravelTimeFromStart(busStationList: PublicGyeonggiResponse.BusRouteStationListResponse): Long {
        // 1) 우선순위에 따라 사용할 station 이름 결정
        val chosenStationName =
            when {
                stationNm2.isNotBlank() -> stationNm2
                stationNm1.isNotBlank() -> stationNm1
                else -> ""
            }

        // 2) 두 스테이션 모두 이름이 비어 있으면 기본(평균 2초/정류장) 사용
        if (chosenStationName.isBlank()) {
            val stationCount = if (staOrder < turnSeq) staOrder else staOrder - turnSeq
            return (2 * stationCount).toLong()
        }

        // 3) 실제 스테이션 정보 조회
        val chosenStation = busStationList.getStation(chosenStationName, staOrder)
        val remainOrder = staOrder - chosenStation.stationSeq

        // 4) 스테이션에 따라 예측 시간(predictTimeSec)도 정해줌
        val chosenPredictTimeSec =
            when (chosenStationName) {
                stationNm2 -> predictTimeSec2
                stationNm1 -> predictTimeSec1
                else -> null
            }

        // 5) 평균 이동 시간 계산 (remainOrder가 0이거나 예측시간이 null이면 2초 기본값)
        val averageSecondPerStation =
            if (chosenPredictTimeSec != null && remainOrder != 0) {
                chosenPredictTimeSec.div(remainOrder)
            } else {
                2
            }

        // 6) 구간별 정류장 수
        val stationCount = if (staOrder < turnSeq) staOrder else (staOrder - turnSeq)

        // 최종 이동 시간 (초)
        return (averageSecondPerStation * stationCount).toLong()
    }

    private fun createRealTimeArrivalInfo(
        predictTimeSec: Int?,
        crowded: Int,
        remainSeatCnt: Int,
        vehId: Int
    ): RealTimeBusArrival {
        val busCongestion =
            when (crowded) {
                0 -> BusCongestion.UNKNOWN
                1 -> BusCongestion.LOW
                2 -> BusCongestion.MEDIUM
                3 -> BusCongestion.HIGH
                4 -> BusCongestion.VERY_HIGH
                else -> throw IllegalArgumentException("Unknown bus congestion: $crowded")
            }

        val remainingSeats = if (busCongestion == BusCongestion.LOW && remainSeatCnt == 0) null else remainSeatCnt

        return RealTimeBusArrival(
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
    val regionName: String,
    val stationId: String,
    val stationName: String,
    val x: String,
    val y: String,
    val stationSeq: Int,
    val turnSeq: Int,
    val turnYn: String
) {
    fun getDirection(): BusDirection {
        return if (stationSeq < turnSeq) BusDirection.DOWN else BusDirection.UP
    }

    fun toBusRouteStation(busRoute: BusRoute): BusRouteStation {
        return BusRouteStation(
            busRoute = busRoute,
            busStation =
                BusStation(
                    id = BusStationId(stationId),
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

class GyeonggiBusStation(
    val stationId: String,
    val stationName: String,
    val x: String,
    val y: String
) {
    fun toBusStation(): BusStation {
        return BusStation(
            id = BusStationId(stationId),
            busStationMeta =
                BusStationMeta(
                    name = stationName,
                    coordinate = Coordinate(y.toDouble(), x.toDouble())
                )
        )
    }
}

data class GyeonggiBusRoute(
    val routeId: String,
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
    val routeId: String,
    val routeName: String,
    val startStationId: String,
    val startStationName: String,
    val endStationId: String,
    val endStationName: String,
    val turnStID: String,
    val turnStNm: String,
    // 첫차 시간 필드
    val upFirstTime: String?,
    val downFirstTime: String?,
    val satUpFirstTime: String?,
    val satDownFirstTime: String?,
    val sunUpFirstTime: String?,
    val sunDownFirstTime: String?,
    val weUpFirstTime: String?,
    val weDownFirstTime: String?,
    // 막차 시간 필드
    val upLastTime: String?,
    val downLastTime: String?,
    val satUpLastTime: String?,
    val satDownLastTime: String?,
    val sunUpLastTime: String?,
    val sunDownLastTime: String?,
    val weUpLastTime: String?,
    val weDownLastTime: String?,
    val peekAlloc: Int,
    val nPeekAlloc: Int,
    val wePeekAlloc: Int,
    val weNPeekAlloc: Int,
    val satPeekAlloc: Int,
    val satNPeekAlloc: Int,
    val sunPeekAlloc: Int,
    val sunNPeekAlloc: Int
) {
    // 버스 시간 타입 정의
    enum class BusTimeType {
        FIRST,
        LAST
    }

    fun toBusArrival(
        dailyType: DailyType,
        busDirection: BusDirection,
        busArrivalInfo: BusArrivalItem,
        busStationList: PublicGyeonggiResponse.BusRouteStationListResponse
    ): BusArrival {
        val firstTime = getBusTime(dailyType, busDirection, BusTimeType.FIRST)
        val lastTime = getBusTime(dailyType, busDirection, BusTimeType.LAST)
        val term = getTerm(dailyType)

        val travelTimeFromStart = busArrivalInfo.calculateTravelTimeFromStart(busStationList)

        return BusArrival(
            busRoute =
                BusRoute(
                    id = BusRouteId(routeId),
                    name = routeName,
                    serviceRegion = ServiceRegion.GYEONGGI
                ),
            busStationId = BusStationId(busArrivalInfo.stationId),
            stationName = busStationList.getStation(BusStationId(busArrivalInfo.stationId)).stationName,
            firstTime = firstTime.plusSeconds(travelTimeFromStart),
            lastTime = lastTime.plusSeconds(travelTimeFromStart),
            term = term,
            realTimeInfo = busArrivalInfo.toRealTimeBussArrivals()
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

    // 통합된 시간 조회 함수
    private fun getBusTime(
        dailyType: DailyType,
        busDirection: BusDirection,
        timeType: BusTimeType
    ): LocalDateTime {
        val timeStr: String =
            getTimeString(dailyType, busDirection, timeType)
                ?: throw IllegalArgumentException("첫차 또는 막차 시간을 가져올 수 없습니다.")

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

    // 시간 문자열 파싱 함수
    private fun parseTime(timeStr: String): LocalDateTime {
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
            throw IllegalArgumentException("올바르지 않은 시간 형식입니다: $timeStr")
        }
    }

    // 서비스 시간 생성 함수
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
    val crowded: Int,
    val plateNo: String,
    val remainSeatCnt: Int,
    val routeId: Int,
    val stationId: Int,
    val stationSeq: Int,
    val vehId: Int
) {
    fun toBusPosition(): BusPosition {
        val busCongestion =
            when (crowded) {
                1 -> BusCongestion.LOW
                2 -> BusCongestion.MEDIUM
                3 -> BusCongestion.HIGH
                4 -> BusCongestion.VERY_HIGH
                else -> throw IllegalArgumentException("Unknown bus congestion: $crowded")
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
