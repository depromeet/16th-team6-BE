package com.deepromeet.atcha.transit.api.response

import com.deepromeet.atcha.location.domain.ServiceRegion
import com.deepromeet.atcha.transit.domain.bus.BusArrivalInfo
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
        busArrivalInfo: BusArrivalInfo
    ) : this(
        busArrivalInfo.schedule.busRouteInfo.route.id,
        busArrivalInfo.schedule.busRouteInfo.route.name,
        busArrivalInfo.schedule.busRouteInfo.route.serviceRegion,
        busArrivalInfo.schedule.busStation.id,
        busArrivalInfo.schedule.busStation.busStationMeta.name,
        busArrivalInfo.schedule.busTimeTable.lastTime,
        busArrivalInfo.schedule.busTimeTable.term,
        busArrivalInfo.realTimeArrival.realTimeInfoList.map { RealTimeBusArrivalResponse(it) }
    )
}
