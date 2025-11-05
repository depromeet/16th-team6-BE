package com.deepromeet.atcha.transit.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.subway.SubwayStationBatchAppender
import com.deepromeet.atcha.transit.domain.Fare
import com.deepromeet.atcha.transit.domain.RoutePassStops
import com.deepromeet.atcha.transit.domain.bus.BusArrivalInfo
import com.deepromeet.atcha.transit.domain.bus.BusRoute
import com.deepromeet.atcha.transit.domain.bus.BusRouteOperationInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import org.springframework.stereotype.Service

@Service
class TransitService(
    private val taxiFareFetcher: TaxiFareFetcher,
    private val busManager: BusManager,
    private val subwayStationBatchAppender: SubwayStationBatchAppender
) {
    suspend fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end)
    }

    suspend fun getBusArrival(
        routeName: String,
        busStationMeta: BusStationMeta,
        passStopList: RoutePassStops
    ): BusArrivalInfo {
        val schedule = busManager.getSchedule(routeName, busStationMeta, passStopList)
        val realTimeArrival = busManager.getRealTimeArrival(routeName, busStationMeta, passStopList)
        return BusArrivalInfo(schedule, realTimeArrival)
    }

    suspend fun getBusPositions(busRoute: BusRoute) = busManager.getBusPositions(busRoute)

    suspend fun getBusOperationInfo(busRoute: BusRoute): BusRouteOperationInfo {
        return busManager.getBusRouteOperationInfo(busRoute)
    }

    suspend fun init() {
        subwayStationBatchAppender.appendAll()
    }
}
