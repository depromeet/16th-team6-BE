package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.RouteItinerary
import com.deepromeet.atcha.route.domain.RouteLeg
import com.deepromeet.atcha.route.domain.RouteMode
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.subway.SubwayManager
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * V2 – "데모용(실시간 시연)" 경로 계산 컴포넌트.
 * ▶ 실제 현재 시각/실시간 위치와 무관.
 * ▶ 하지만 **TransitInfo(SubwayTimeInfo / BusTimeInfo)** 는 채워 넣어 UI 정보 표시를 유지한다.
 * ▶ departureDateTime 은 "현재 시각 + 5 분" 부터 sectionTime 누적.
 */
@Component
class LastRouteCalculatorV2(
    private val subwayManager: SubwayManager,
    private val busManager: BusManager,
    private val lastRouteAppender: LastRouteAppender
) {
    suspend fun calculateRoutesV2(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<RouteItinerary>,
        time: Int
    ): List<LastRoute> {
        val routes =
            coroutineScope {
                itineraries.map { itinerary ->
                    async(Dispatchers.Default) {
                        calculateRouteDemo(itinerary, time)
                    }
                }
                    .awaitAll()
                    .filterNotNull()
            }

        if (routes.isNotEmpty()) {
            lastRouteAppender.appendRoutes(start, destination, routes)
        }

        log.info { "[V2‑DEMO] 계산 완료: ${routes.size}/${itineraries.size}" }
        return routes
    }

    private suspend fun calculateRouteDemo(
        itinerary: RouteItinerary,
        time: Int
    ): LastRoute? {
        try {
            val legs = buildDemoLegs(itinerary.legs, time) ?: return null
            val walkFixed = increaseWalkTime(legs)
            val departAt = calculateDepartureDateTime(walkFixed)
            val totalSec = calculateTotalTime(walkFixed, departAt)

            return LastRoute(
                id = UUID.randomUUID().toString(),
                departureDateTime =
                    departAt
                        .truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                totalTime = totalSec.toInt(),
                totalWalkTime = itinerary.totalWalkTime,
                transferCount = itinerary.transferCount,
                totalWalkDistance = itinerary.totalWalkDistance,
                totalDistance = itinerary.totalDistance,
                pathType = itinerary.pathType,
                legs = walkFixed
            )
        } catch (e: Exception) {
            log.error(e) { "[V2‑DEMO] 경로 계산 중 오류 발생" }
            return null
        }
    }

    /**
     * sectionTime 누적 + 각 레그는 **시간표 조회** 하여 TransitInfo 채운다.
     * ─ departureDateTime 은 현재시각+5분부터 누적, 시간표 기준과 무관.
     */
    private suspend fun buildDemoLegs(
        legs: List<RouteLeg>,
        time: Int
    ): List<LastRouteLeg>? {
        var cursor = LocalDateTime.now().plusMinutes(time.toLong())
        val result = mutableListOf<LastRouteLeg>()

        for (leg in legs) {
            val lastRouteLeg: LastRouteLeg =
                when (leg.mode) {
                    RouteMode.SUBWAY -> {
                        try {
                            val subwayLine = SubwayLine.Companion.fromRouteName(leg.route!!)
                            val routes = subwayManager.getRoutes(subwayLine)
                            val startStation = subwayManager.getStation(subwayLine, leg.start.name)
                            val endStation = subwayManager.getStation(subwayLine, leg.end.name)
                            val nextStation = subwayManager.getStation(subwayLine, leg.passStops!!.getNextStationName())
                            val timeTable = subwayManager.getTimeTable(startStation, nextStation, endStation, routes)

                            leg.toLastTransitLeg(
                                departureDateTime =
                                    cursor
                                        .truncatedTo(ChronoUnit.SECONDS)
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                                transitInfo = TransitInfo.SubwayInfo(subwayLine, timeTable, timeTable.schedules[0])
                            )
                        } catch (e: Exception) {
                            log.warn(e) { "지하철 정보 조회 실패, 기본값 사용: ${leg.route}, ${leg.start.name}, ${leg.end.name}" }
                            return null
                        }
                    }

                    RouteMode.BUS -> {
                        try {
                            val routeId = leg.resolveRouteName()
                            val stationMeta = leg.toBusStationMeta()

                            val busSchedule = busManager.getSchedule(routeId, stationMeta, leg.passStops!!)

                            leg.toLastTransitLeg(
                                departureDateTime =
                                    cursor
                                        .truncatedTo(ChronoUnit.SECONDS)
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                                transitInfo = TransitInfo.BusInfo(busSchedule)
                            )
                        } catch (e: Exception) {
                            log.warn(e) { "버스 정보 조회 실패, 기본값 사용: ${leg.route}" }
                            return null
                        }
                    }

                    else -> leg.toLastWalkLeg()
                }

            result.add(lastRouteLeg)
            cursor = cursor.plusSeconds(leg.sectionTime.toLong())
        }

        return result
    }

    /**
     * 도보 시간 증가 (2분 추가)
     */
    private fun increaseWalkTime(legs: List<LastRouteLeg>): List<LastRouteLeg> {
        return legs.mapIndexed { index, currentLeg ->
            val nextLeg = legs.getOrNull(index + 1)
            if (currentLeg.isWalk() && nextLeg?.isTransit() == true) {
                currentLeg.copy(sectionTime = currentLeg.sectionTime + 120)
            } else {
                currentLeg
            }
        }
    }

    /**
     * 출발 시간 계산
     */
    private fun calculateDepartureDateTime(legs: List<LastRouteLeg>): LocalDateTime {
        val firstTransitIndex = legs.indexOfFirst { it.isTransit() }
        if (firstTransitIndex == -1) {
            // 대중교통이 없는 경우 (전체 도보)
            return LocalDateTime.now().plusMinutes(5)
        }

        val firstTransit = legs[firstTransitIndex]
        val departureDateTime = LocalDateTime.parse(firstTransit.departureDateTime!!)
        val totalWalkTime =
            if (firstTransitIndex > 0) {
                legs.subList(0, firstTransitIndex)
                    .filter { it.isWalk() }
                    .sumOf { it.sectionTime.toLong() }
            } else {
                0L
            }

        return departureDateTime.minusSeconds(totalWalkTime)
    }

    /**
     * 총 소요 시간 계산
     */
    private fun calculateTotalTime(
        legs: List<LastRouteLeg>,
        departureDateTime: LocalDateTime
    ): Long {
        val lastTransitIndex = legs.indexOfLast { it.isTransit() }
        if (lastTransitIndex == -1) {
            // 대중교통이 없는 경우 (전체 도보)
            return legs.sumOf { it.sectionTime.toLong() }
        }

        val lastTransit = legs[lastTransitIndex]
        val lastTransitDepartureTime = LocalDateTime.parse(lastTransit.departureDateTime!!)
        var arrivalTime = lastTransitDepartureTime.plusSeconds(lastTransit.sectionTime.toLong())

        val totalWalkTime =
            legs.drop(lastTransitIndex + 1)
                .filter { it.isWalk() }
                .sumOf { it.sectionTime.toLong() }

        arrivalTime = arrivalTime.plusSeconds(totalWalkTime)
        return Duration.between(departureDateTime, arrivalTime).seconds
    }
}
