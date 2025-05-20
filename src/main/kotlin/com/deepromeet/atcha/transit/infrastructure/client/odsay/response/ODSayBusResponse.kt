package com.deepromeet.atcha.transit.infrastructure.client.odsay.response

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusCongestion
import com.deepromeet.atcha.transit.domain.BusRoute
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusStationId
import com.deepromeet.atcha.transit.domain.BusStatus
import com.deepromeet.atcha.transit.domain.BusTimeTable
import com.deepromeet.atcha.transit.domain.RealTimeBusArrival
import com.deepromeet.atcha.transit.domain.ServiceRegion
import java.time.LocalDateTime
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
    var businfo: ODSayBusinfoResponse
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
    var localStations: List<ODSayLocalStationResponse>
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
    fun toBusArrival(): BusArrival {
        var busRoute =
            BusRoute(
                id = BusRouteId(this.busLocalBlID),
                name = this.busNo,
                serviceRegion = ServiceRegion.SEOUL
            )
        return BusArrival(
            busRoute = busRoute,
            // TODO : 오딧세이에서는 버스 스테이션 id 없음 수정 예정
            busStationId = BusStationId(""),
            busTimeTable =
                BusTimeTable(
                    toBusArrivalTime(busFirstTime),
                    toBusArrivalTime(busLastTime),
                    busInterval.toInt()
                ),
            realTimeInfo =
                listOf(
                    RealTimeBusArrival(
                        vehicleId = "",
                        busStatus = BusStatus.SOON,
                        remainingTime = 0,
                        remainingStations = 0,
                        isLast = false,
                        busCongestion = BusCongestion.LOW,
                        remainingSeats = 0
                    )
                ),
            stationName = ""
        )
    }

    // TODO : "xx:xx" -> LocalDateTime 변환, Util로 옮기기
    fun toBusArrivalTime(time: String): LocalDateTime {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
}

data class ODSayLocalStationResponse(
    var cityCode: Long,
    var localStationID: String
)
