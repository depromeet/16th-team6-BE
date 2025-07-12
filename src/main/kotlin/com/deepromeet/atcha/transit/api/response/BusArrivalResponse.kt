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
    val lastTime: LocalDateTime?,
    val term: Int,
    val realTimeBusArrival: List<RealTimeBusArrivalResponse>
) {
    constructor(
        busArrival: BusArrival
    ) : this(
        busArrival.schedule.busRoute.id,
        busArrival.schedule.busRoute.name,
        busArrival.schedule.busRoute.serviceRegion,
        busArrival.schedule.busStation.id,
        busArrival.schedule.busStation.busStationMeta.name,
        busArrival.schedule.busTimeTable.lastTime,
        busArrival.schedule.busTimeTable.term,
        busArrival.realTimeArrival.realTimeInfoList.map { RealTimeBusArrivalResponse(it) }
    )
}
