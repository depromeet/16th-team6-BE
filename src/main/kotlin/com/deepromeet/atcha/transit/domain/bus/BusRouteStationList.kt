package com.deepromeet.atcha.transit.domain.bus

import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException

data class BusRouteStationList(
    val busRouteStations: List<BusRouteStation>,
    val turnPoint: Int?
) {
    fun getTargetStationById(busStationId: BusStationId): BusRouteStation {
        return busRouteStations.firstOrNull { it.busStation.id == busStationId }
            ?: throw TransitException.of(
                TransitError.NOT_FOUND_BUS_STATION,
                "${busStationId.value}에 해당하는 정류장를 ${busRouteStations[0].busRoute.id.value} 노선의 경유 정류장 리스트에서 찾을 수 없습니다."
            )
    }
}
