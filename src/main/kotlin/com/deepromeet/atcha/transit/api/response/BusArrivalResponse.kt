package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.bus.BusArrival
import com.deepromeet.atcha.transit.domain.bus.BusRouteId
import com.deepromeet.atcha.transit.domain.bus.BusStationId
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
        busArrival.schedule.busRouteInfo.route.id,
        busArrival.schedule.busRouteInfo.route.name,
        busArrival.schedule.busRouteInfo.route.serviceRegion,
        busArrival.schedule.busStation.id,
        busArrival.schedule.busStation.busStationMeta.name,
        busArrival.schedule.busTimeTable.lastTime,
        busArrival.schedule.busTimeTable.term,
        busArrival.realTimeArrival.realTimeInfoList.map { RealTimeBusArrivalResponse(it) }
    )
}
