package com.deepromeet.atcha.transit.infrastructure.client.odsay.response

import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusSchedule
import com.deepromeet.atcha.transit.domain.BusStation
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.ServiceRegion
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    fun toBusArrival(station: BusStation): BusSchedule {
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
                    toBusArrivalTime(busFirstTime),
                    toBusArrivalTime(busLastTime),
                    busInterval.toInt()
                )
        )
    }

    // TODO : "xx:xx" -> LocalDateTime 변환, Util로 옮기기
    fun toBusArrivalTime(time: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val overDay = time.substring(0, 2).toInt() > 24
        val checkTime =
            if (overDay) {
                "0" + time.substring(0, 2).toInt().minus(24).toString() + time.substring(2)
            } else {
                time
            }
        val localTime = LocalTime.parse(checkTime, formatter)
        if (overDay) {
            return LocalDateTime.of(LocalDate.now(), localTime)
                .plusDays(1)
        }
        return LocalDateTime.of(LocalDate.now(), localTime)
    }
}

data class ODSayLocalStationResponse(
    var cityCode: Long,
    var localStationID: String
)
