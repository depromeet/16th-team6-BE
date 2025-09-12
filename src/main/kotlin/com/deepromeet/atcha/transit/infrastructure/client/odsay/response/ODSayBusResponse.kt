package com.deepromeet.atcha.transit.infrastructure.client.odsay.response

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.domain.bus.BusRouteInfo
import com.deepromeet.atcha.transit.domain.bus.BusSchedule
import com.deepromeet.atcha.transit.domain.bus.BusTimeTable
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.lang.Exception
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ODSayBusArrivalResponse(
    val result: ODSayBusArrivalResponseResult
)

data class ODSayBusArrivalResponseResult(
    var totalCount: Long,
    var station: List<ODSayStationResponse>
)

data class ODSayStationResponse(
    var stationClass: String,
    var stationName: String,
    var stationID: String,
    var x: Double,
    var y: Double,
    var CID: Long,
    var cityName: String,
    var arsID: String,
    var businfo: List<ODSayBusinfoResponse> = emptyList()
)

data class ODSayBusinfoResponse(
    var busLocalBlID: String,
    var busNo: String
)

data class ODSayBusStationInfoResponse(
    var result: ODSayBusStationInfoResponseResult
)

data class ODSayBusStationInfoResponseResult(
    var stationName: String,
    var stationID: Long,
    var x: Double,
    var y: Double,
    var localStationID: String,
    var stationCityCode: String,
    var arsID: String,
    var lane: List<ODSayLaneResponse>,
    var localStations: List<ODSayLocalStationResponse> = emptyList()
)

data class ODSayLaneResponse(
    var busNo: String,
    var busID: Long,
    var busStartPoint: String,
    var busEndPoint: String,
    var busFirstTime: String,
    var busLastTime: String,
    var busInterval: String,
    var busLocalBlID: String
) {
    fun toBusSchedule(busRouteInfo: BusRouteInfo): BusSchedule {
        val busRoute =
            BusRoute(
                id = BusRouteId(this.busLocalBlID),
                name = this.busNo,
                serviceRegion = ServiceRegion.SEOUL
            )
        return BusSchedule(
            busRouteInfo = busRouteInfo,
            busStation = busRouteInfo.targetStation.busStation,
            busTimeTable =
                BusTimeTable(
                    parseTime(busFirstTime, LocalDate.now()),
                    parseTime(busLastTime, LocalDate.now()),
                    parseIntervalFromString(busInterval)
                )
        )
    }

    private fun parseIntervalFromString(intervalStr: String): Int {
        return try {
            when {
                intervalStr.endsWith("회") -> {
                    // 운행횟수로 온 경우 배차간격으로 변환
                    val operationCount = intervalStr.removeSuffix("회").toInt()
                    calculateIntervalFromOperationCount(operationCount)
                }
                else -> {
                    intervalStr.toInt()
                }
            }
        } catch (e: NumberFormatException) {
            throw TransitException.of(TransitError.INVALID_TIME_FORMAT)
        }
    }

    private fun calculateIntervalFromOperationCount(operationCount: Int): Int {
        if (operationCount <= 1) return 0

        val referenceDate = LocalDate.now()
        val firstDateTime = parseTime(busFirstTime, referenceDate)
        val lastDateTime = parseTime(busLastTime, referenceDate)

        val totalMinutes = Duration.between(firstDateTime, lastDateTime).toMinutes()

        // 운행횟수 - 1로 나누는 이유: 첫차와 막차 사이의 간격 개수
        return (totalMinutes / (operationCount - 1)).toInt()
    }

    fun parseTime(
        timeStr: String?,
        referenceDate: LocalDate
    ): LocalDateTime {
        return try {
            if (timeStr.isNullOrBlank()) {
                throw TransitException.of(TransitError.INVALID_TIME_FORMAT)
            }

            val parts = timeStr.split(":")
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()

            var date = referenceDate

            if (hour >= 24) {
                hour -= 24
                date = referenceDate.plusDays(1)
            }

            val localTime = LocalTime.of(hour, minute)
            LocalDateTime.of(date, localTime)
        } catch (e: Exception) {
            throw TransitException.of(TransitError.INVALID_TIME_FORMAT)
        }
    }
}

data class ODSayLocalStationResponse(
    var cityCode: Long,
    var localStationID: String
)
