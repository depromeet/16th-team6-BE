package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.RouteLeg
import com.deepromeet.atcha.route.domain.RouteMode
import com.deepromeet.atcha.route.exception.RouteError
import com.deepromeet.atcha.route.exception.RouteException
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.subway.SubwayManager
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class LastRouteLegCalculator(
    private val subwayManager: SubwayManager,
    private val busManager: BusManager
) {
    suspend fun calcLastTime(legs: List<RouteLeg>): List<LastRouteLeg> {
        return coroutineScope {
            legs.map { leg ->
                async {
                    when (leg.mode) {
                        RouteMode.SUBWAY -> calculateSubwayLeg(leg)
                        RouteMode.BUS -> calculateBusLeg(leg)
                        RouteMode.WALK -> leg.toLastWalkLeg()
                        else -> throw RouteException.of(
                            RouteError.INVALID_ROUTE_MODE,
                            "${leg.mode}는 지원하지 않는 모드입니다."
                        )
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    private suspend fun calculateSubwayLeg(leg: RouteLeg): LastRouteLeg {
        return try {
            coroutineScope {
                val subwayLine = SubwayLine.fromRouteName(leg.route!!)

                val routesAsync = async { subwayManager.getRoutes(subwayLine) }
                val startAsync = async { subwayManager.getStation(subwayLine, leg.start.name) }
                val nextAsync = async { subwayManager.getStation(subwayLine, leg.passStops!!.getNextStationName()) }
                val destinationAsync = async { subwayManager.getStation(subwayLine, leg.end.name) }

                val routes = routesAsync.await()
                val start = startAsync.await()
                val next = nextAsync.await()
                val destination = destinationAsync.await()

                val timeTable = subwayManager.getTimeTable(start, next, destination, routes, leg.isExpress())
                val lastSchedule = timeTable.getLastTime()

                leg.toLastTransitLeg(
                    departureDateTime = lastSchedule.departureTime.toLocalDateTime(),
                    transitInfo = TransitInfo.SubwayInfo(subwayLine, timeTable)
                )
            }
        } catch (e: Exception) {
            log.debug(e) { "❌ 지하철 막차 시간 계산 실패 - 노선: ${leg.route}, 출발역: ${leg.start.name}, 도착역: ${leg.end.name}" }
            throw e
        }
    }

    private suspend fun calculateBusLeg(leg: RouteLeg): LastRouteLeg {
        val routeName = leg.resolveRouteName()
        val stationMeta = leg.toBusStationMeta()

        val busSchedule = busManager.getSchedule(routeName, stationMeta, leg.passStops!!)
        val lastDepartureTime = busSchedule.busTimeTable.lastTime

        return leg.toLastTransitLeg(
            departureDateTime = lastDepartureTime,
            transitInfo = TransitInfo.BusInfo(busSchedule)
        )
    }
}
