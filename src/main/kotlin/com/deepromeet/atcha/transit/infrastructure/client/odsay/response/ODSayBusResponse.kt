package com.deepromeet.atcha.transit.infrastructure.client.odsay.response

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.ServiceRegion
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.lang.Exception
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
    fun toBusSchedule(station: BusStation): BusSchedule {
        val busRoute =
            BusRoute(
                id = BusRouteId(this.busLocalBlID),
                name = this.busNo,
                serviceRegion = ServiceRegion.SEOUL
            )
        return BusSchedule(
            busRoute = busRoute,
            busStation = station,
            busTimeTable =
                BusTimeTable(
                    parseTime(busFirstTime, LocalDate.now()),
                    parseTime(busLastTime, LocalDate.now()),
                    busInterval.toInt()
                )
        )
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
