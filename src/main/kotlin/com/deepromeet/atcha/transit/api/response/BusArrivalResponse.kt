package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.transit.domain.BusArrival
import com.deepromeet.atcha.transit.domain.BusRouteId
import com.deepromeet.atcha.transit.domain.BusStationId
import com.deepromeet.atcha.transit.domain.ServiceRegion
import java.time.LocalDateTime

data class BusArrivalResponse(
    val busRouteId: BusRouteId,
    val routeName: String,
    val serviceRegion: ServiceRegion,
    val busStationId: BusStationId,
    val stationName: String,
    val lastTime: LocalDateTime,
    val term: Int,
    val realTimeBusArrival: List<RealTimeBusArrivalResponse>
) {
    constructor(
        busArrival: BusArrival
    ) : this(
        busArrival.busRoute.id,
        busArrival.busRoute.name,
        busArrival.busRoute.serviceRegion,
        busArrival.busStationId,
        busArrival.stationName,
        busArrival.lastTime,
        busArrival.term,
        busArrival.realTimeInfo.map { RealTimeBusArrivalResponse(it) }
    )
}
