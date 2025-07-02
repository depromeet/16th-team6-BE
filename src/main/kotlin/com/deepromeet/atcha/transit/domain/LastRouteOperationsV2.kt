package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Leg
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

/**
 * V2 – "데모용(실시간 시연)" 경로 계산 컴포넌트.
 * ▶ 실제 현재 시각/실시간 위치와 무관.
 * ▶ 하지만 **TransitInfo(SubwayTimeInfo / BusTimeInfo)** 는 채워 넣어 UI 정보 표시를 유지한다.
 * ▶ departureDateTime 은 "현재 시각 + 5 분" 부터 sectionTime 누적.
 */
@Component
class LastRouteOperationsV2(
    private val subwayManager: SubwayManager,
    private val busManager: BusManager,
    private val lastRouteAppender: LastRouteAppender
) {
    private val log = KotlinLogging.logger {}

    suspend fun calculateRoutesV2(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<Itinerary>
    ): List<LastRoute> {
        val routes =
            coroutineScope {
                itineraries.map { itin -> async(Dispatchers.Default) { calculateRouteDemo(itin) } }
                    .awaitAll().filterNotNull()
            }
        if (routes.isNotEmpty()) lastRouteAppender.appendRoutes(start, destination, routes)
        log.info { "[V2‑DEMO] 계산 완료: ${routes.size}/${itineraries.size}" }
        return routes
    }

    private suspend fun calculateRouteDemo(itinerary: Itinerary): LastRoute? {
        try {
            val legs = buildDemoLegs(itinerary.legs) ?: return null
            val walkFixed = increaseWalkTime(legs)
            val departAt = calculateDepartureDateTime(walkFixed)
            val totalSec = calculateTotalTime(walkFixed, departAt)
            return LastRoute(
                routeId = UUID.randomUUID().toString(),
                departureDateTime = departAt.toString(),
                totalTime = totalSec.toInt(),
                totalWalkTime = itinerary.totalWalkTime,
                transferCount = itinerary.transferCount,
                totalWorkDistance = itinerary.totalWalkDistance,
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
     *  ─ departureDateTime 은 현재시각+5분부터 누적, 시간표 기준과 무관.
     */
    private suspend fun buildDemoLegs(legs: List<Leg>): List<LastRouteLeg>? {
        var cursor = LocalDateTime.now().plusMinutes(5)
        val result = mutableListOf<LastRouteLeg>()

        for (leg in legs) {
            val lr: LastRouteLeg =
                when (leg.mode) {
                    "SUBWAY" -> {
                        val line = SubwayLine.fromRouteName(leg.route!!)
                        val (routes, startSta, endSta) =
                            run {
                                val r = subwayManager.getRoutes(line)
                                val s = subwayManager.getStation(line, leg.start.name)
                                val e = subwayManager.getStation(line, leg.end.name)
                                Triple(r, s, e)
                            }
                        val tt = subwayManager.getTimeTable(startSta, endSta, routes)
                        leg.toLastRouteLeg(cursor, TransitInfo.SubwayInfo(tt))
                    }
                    "BUS" -> {
                        val routeId = leg.route!!.split(":")[1]
                        val stationMeta =
                            BusStationMeta(leg.start.name.removeSuffix(), Coordinate(leg.start.lat, leg.start.lon))
                        val busSchedule =
                            busManager.getSchedule(
                                routeId,
                                stationMeta,
                                leg.passStopList!!
                            )
                        leg.toLastRouteLeg(cursor, TransitInfo.BusInfo(busSchedule))
                    }
                    else -> leg.toLastRouteLeg(cursor, TransitInfo.NoInfoTable)
                }
            result += lr
            cursor = cursor.plusSeconds(leg.sectionTime.toLong())
        }
        return result
    }

    // =============  UTILITIES (V1 로직 재사용)  =============
    private fun increaseWalkTime(legs: List<LastRouteLeg>): List<LastRouteLeg> =
        legs.mapIndexed { i, l ->
            val next = legs.getOrNull(i + 1)
            if (l.mode == "WALK" && (next?.mode == "SUBWAY" || next?.mode == "BUS")) {
                l.copy(sectionTime = l.sectionTime + 120)
            } else {
                l
            }
        }

    private fun calculateDepartureDateTime(legs: List<LastRouteLeg>): LocalDateTime {
        val idx = legs.indexOfFirst { it.mode != "WALK" }
        val first = legs[idx]
        val dep = LocalDateTime.parse(first.departureDateTime!!)
        val preWalk =
            if (idx > 0) {
                legs.subList(
                    0,
                    idx
                ).filter { it.mode == "WALK" }.sumOf { it.sectionTime.toLong() }
            } else {
                0L
            }
        return dep.minusSeconds(preWalk)
    }

    private fun calculateTotalTime(
        legs: List<LastRouteLeg>,
        dep: LocalDateTime
    ): Long {
        val lastIdx = legs.indexOfLast { it.mode != "WALK" }
        val last = legs[lastIdx]
        var arrive = LocalDateTime.parse(last.departureDateTime!!).plusSeconds(last.sectionTime.toLong())
        val postWalk = legs.drop(lastIdx + 1).filter { it.mode == "WALK" }.sumOf { it.sectionTime.toLong() }
        arrive = arrive.plusSeconds(postWalk)
        return Duration.between(dep, arrive).seconds
    }

    // ----- helpers -----
    private fun Leg.toLastRouteLeg(
        dt: LocalDateTime?,
        info: TransitInfo
    ): LastRouteLeg =
        LastRouteLeg(
            distance = distance,
            sectionTime = sectionTime,
            mode = mode,
            departureDateTime = dt?.toString(),
            route = route,
            type = type.toString(),
            service = service.toString(),
            start = start,
            end = end,
            passStopList = passStopList,
            step = steps,
            passShape = passShape?.linestring,
            transitInfo = info
        )

    private fun String.removeSuffix() = replace("(중)", "").trim()
}
