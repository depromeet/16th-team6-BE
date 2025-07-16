package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.RouteLeg
import com.deepromeet.atcha.route.domain.RouteMode
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.subway.SubwayManager
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class LastRouteLegCalculator(
    private val subwayManager: SubwayManager,
    private val busManager: BusManager
) {
    suspend fun calcWithLastTime(legs: List<RouteLeg>): List<LastRouteLeg>? {
        return coroutineScope {
            legs.map { leg ->
                async(Dispatchers.Default) {
                    when (leg.mode) {
                        RouteMode.SUBWAY -> calculateSubwayLeg(leg)
                        RouteMode.BUS -> calculateBusLeg(leg)
                        else -> leg.toLastWalkLeg()
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun calculateSubwayLeg(leg: RouteLeg): LastRouteLeg {
        return coroutineScope {
            val subwayLine = SubwayLine.fromRouteName(leg.route!!)

            // 병렬로 필요한 데이터 조회
            val routesDeferred = async { subwayManager.getRoutes(subwayLine) }
            val startStationDeferred = async { subwayManager.getStation(subwayLine, leg.start.name) }
            val endStationDeferred = async { subwayManager.getStation(subwayLine, leg.end.name) }

            val routes = routesDeferred.await()
            val startStation = startStationDeferred.await()
            val endStation = endStationDeferred.await()

            // 시간표 조회 및 막차 시간 추출
            val timeTable = subwayManager.getTimeTable(startStation, endStation, routes)
            val lastDepartureTime = timeTable.getLastTime(endStation, routes, leg.isExpress())
            val transitInfo = TransitInfo.SubwayInfo(timeTable)

            leg.toLastTransitLeg(
                departureDateTime = lastDepartureTime.departureTime.toString(),
                transitInfo = transitInfo
            )
        }
    }

    private suspend fun calculateBusLeg(leg: RouteLeg): LastRouteLeg {
        val routeId = leg.resolveRouteName()
        val stationMeta = leg.toBusStationMeta()

        // 버스 스케줄 조회
        val busSchedule = busManager.getSchedule(routeId, stationMeta, leg.passStops!!)
        val lastDepartureTime = busSchedule.busTimeTable.lastTime
        val transitInfo = TransitInfo.BusInfo(busSchedule)

        return leg.toLastTransitLeg(
            departureDateTime = lastDepartureTime.toString(),
            transitInfo = transitInfo
        )
    }
}
