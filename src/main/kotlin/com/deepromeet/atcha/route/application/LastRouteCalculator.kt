package com.deepromeet.atcha.route.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.route.domain.LastRoute
import com.deepromeet.atcha.route.domain.LastRouteLeg
import com.deepromeet.atcha.route.domain.RouteItinerary
import com.deepromeet.atcha.route.domain.RouteLeg
import com.deepromeet.atcha.route.domain.RouteMode
import com.deepromeet.atcha.transit.application.bus.BusManager
import com.deepromeet.atcha.transit.application.subway.SubwayManager
import com.deepromeet.atcha.transit.domain.TimeDirection
import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.subway.SubwayLine
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.cache.LastRouteMetricsRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.Collections
import java.util.UUID

private val log = KotlinLogging.logger {}
private const val MAX_CALCULATION_TIME = 15_000L

@Component
class LastRouteCalculator(
    private val subwayManager: SubwayManager,
    private val busManager: BusManager,
    private val lastRouteAppender: LastRouteAppender,
    private val metricsRepository: LastRouteMetricsRepository
) {
    suspend fun calcLastRoutes(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<RouteItinerary>
    ): List<LastRoute> {
        metricsRepository.incrTotal(itineraries.size.toLong())

        val routes =
            coroutineScope {
                itineraries
                    .map { itinerary ->
                        async(Dispatchers.IO) {
                            withTimeoutOrNull(MAX_CALCULATION_TIME) {
                                calculateRoute(itinerary)
                            }
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }

        if (routes.isNotEmpty()) {
            metricsRepository.incrSuccess(routes.size.toLong())
            lastRouteAppender.appendRoutes(start, destination, routes)
        }

        log.info { "총 ${itineraries.size}개의 여정 중 ${routes.size}개의 막차 경로를 계산했습니다." }
        return routes
    }

    /**
     * 스트리밍 방식 막차 경로 계산
     */
    fun streamLastRoutes(
        start: Coordinate,
        destination: Coordinate,
        itineraries: List<RouteItinerary>
    ): Flow<LastRoute> {
        val lastRouteBuffer = Collections.synchronizedList(mutableListOf<LastRoute>())

        return channelFlow {
            metricsRepository.incrTotal(itineraries.size.toLong())

            for (itinerary in itineraries) {
                launch(Dispatchers.IO) {
                    val route =
                        withTimeoutOrNull(MAX_CALCULATION_TIME) {
                            calculateRoute(itinerary)
                        }

                    if (route != null) {
                        metricsRepository.incrSuccess(1)
                        lastRouteBuffer.add(route)
                        send(route)
                    }
                }
            }
        }.onCompletion {
            log.info { "총 ${itineraries.size}개의 여정 중 ${lastRouteBuffer.size}개의 막차 경로를 계산했습니다." }
            lastRouteAppender.appendRoutes(start, destination, lastRouteBuffer)
        }
    }

    /**
     * 개별 경로의 막차 시간 계산
     */
    private suspend fun calculateRoute(itinerary: RouteItinerary): LastRoute? {
        try {
            // 1. 경로 내 대중교통 별 막차 시간 조회
            val calculatedLegs = calculateLegLastArriveDateTimes(itinerary.legs) ?: return null
            // 2. 도보 시간 조정 - 모든 도보는 2분씩 더해준다.
            val adjustedWalkLegs = increaseWalkTime(calculatedLegs)
            // 3. 막차 시간 기준, 경로 내 대중교통 탑승 가능 여부 확인
            val adjustedLegs = adjustTransitDepartureTimes(adjustedWalkLegs)
            // 4. 유효하지 않는 경로 제거
            if (adjustedLegs.any { leg ->
                    leg.isTransit() && !leg.hasDepartureTime()
                }
            ) {
                return null
            }
            // 5. 출발 시간 계산
            val departureDateTime = calculateDepartureDateTime(adjustedLegs)
            // 6. 총 소요 시간 계산
            val totalTime = calculateTotalTime(adjustedLegs, departureDateTime)

            return LastRoute(
                id = UUID.randomUUID().toString(),
                departureDateTime = departureDateTime.toString(),
                totalTime = totalTime.toInt(),
                totalWalkTime = itinerary.totalWalkTime,
                transferCount = itinerary.transferCount,
                totalWorkDistance = itinerary.totalWalkDistance,
                totalDistance = itinerary.totalDistance,
                pathType = itinerary.pathType,
                legs = adjustedLegs
            )
        } catch (e: Exception) {
            log.warn(e) { "여정의 막차 시간 계산 중 예외가 발생하여 해당 여정을 제외합니다." }
            return null
        }
    }

    private suspend fun calculateLegLastArriveDateTimes(legs: List<RouteLeg>): List<LastRouteLeg>? {
        return coroutineScope {
            legs.map { leg ->
                async(Dispatchers.Default) {
                    when (leg.mode) {
                        RouteMode.SUBWAY -> calculateSubwayLastLeg(leg)
                        RouteMode.BUS -> calculateBusLastLeg(leg)
                        else -> leg.toLastWalkLeg()
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun calculateSubwayLastLeg(leg: RouteLeg): LastRouteLeg =
        coroutineScope {
            val subwayLine = SubwayLine.Companion.fromRouteName(leg.route!!)
            val routesDeferred = async { subwayManager.getRoutes(subwayLine) }
            val startDeferred = async { subwayManager.getStation(subwayLine, leg.start.name) }
            val endDeferred = async { subwayManager.getStation(subwayLine, leg.end.name) }

            val routes = routesDeferred.await()
            val startStation = startDeferred.await()
            val endStation = endDeferred.await()

            val timeTable = subwayManager.getTimeTable(startStation, endStation, routes)
            val departureDateTime = timeTable.getLastTime(endStation, routes, leg.isExpress()).departureTime
            val transitInfo = TransitInfo.SubwayInfo(timeTable)

            leg.toLastTransitLeg(
                departureDateTime = departureDateTime.toString(),
                transitInfo = transitInfo
            )
        }

    private suspend fun calculateBusLastLeg(leg: RouteLeg): LastRouteLeg {
        val routeId = leg.resolveRouteName()
        val stationMeta = leg.toBusStationMeta()

        val busSchedule = busManager.getSchedule(routeId, stationMeta, leg.passStops!!)
        val departureDateTime = busSchedule.busTimeTable.lastTime
        val transitInfo = TransitInfo.BusInfo(busSchedule)

        return leg.toLastTransitLeg(
            departureDateTime = departureDateTime.toString(),
            transitInfo = transitInfo
        )
    }

    /**
     * 도보 시간 증가 (2분 추가)
     */
    private fun increaseWalkTime(legs: List<LastRouteLeg>): List<LastRouteLeg> {
        return legs.mapIndexed { index, currentLeg ->
            val nextLeg = legs.getOrNull(index + 1)
            if (currentLeg.isWalk() && (nextLeg?.isTransit() == true)) {
                currentLeg.copy(sectionTime = currentLeg.sectionTime + 120)
            } else {
                currentLeg
            }
        }
    }

    /**
     * 대중교통 출발 시간 조정
     */
    private fun adjustTransitDepartureTimes(legs: List<LastRouteLeg>): List<LastRouteLeg> {
        val adjustedLegs = legs.toMutableList()
        if (adjustedLegs.any { it.isTransit() && !it.hasDepartureTime() }) return adjustedLegs

        val transitLegs = adjustedLegs.withIndex().filter { it.value.isTransit() }
        if (transitLegs.isEmpty()) return adjustedLegs

        // 1. 대중교통 기준 가장 빠른 막차 시간 찾기
        val earliestTransitLeg = transitLegs.minBy { LocalDateTime.parse(it.value.departureDateTime!!) }

        var isAllRideable = true
        var lastUnrideableIndex: Int? = null

        // 2. 가장 빠른 출발 시간을 기준으로 뒤에 있는 대중교통 탑승 가능 여부 확인
        for (i in earliestTransitLeg.index until adjustedLegs.lastIndex) {
            val currentLeg = adjustedLegs[i]
            if (!currentLeg.isTransit()) continue

            var currentLegAvailableTime =
                LocalDateTime.parse(currentLeg.departureDateTime!!).plusSeconds(currentLeg.sectionTime.toLong())

            var nextIndex = i + 1
            while (nextIndex <= adjustedLegs.lastIndex && adjustedLegs[nextIndex].isWalk()) {
                currentLegAvailableTime =
                    currentLegAvailableTime.plusSeconds(adjustedLegs[nextIndex].sectionTime.toLong())
                nextIndex++
            }

            if (nextIndex > adjustedLegs.lastIndex) break

            val nextLeg = adjustedLegs[nextIndex]
            val nextLegArriveTime = LocalDateTime.parse(nextLeg.departureDateTime!!)

            if (currentLegAvailableTime.isAfter(nextLegArriveTime)) {
                isAllRideable = false
                lastUnrideableIndex = nextIndex
            }
        }

        // 3. 기준점 설정
        val adjustBaseIndex = if (isAllRideable) earliestTransitLeg.index else lastUnrideableIndex!!

        // 4. 기준점 앞쪽 시간 재조정
        var adjustBaseTime = adjustedLegs[adjustBaseIndex].departureDateTime?.let { LocalDateTime.parse(it) }
        for (i in adjustBaseIndex - 1 downTo 0) {
            val leg = adjustedLegs[i]

            if (adjustBaseTime == null) {
                adjustedLegs[i] = leg.copy(departureDateTime = null)
                continue
            }

            if (leg.isWalk()) {
                adjustBaseTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong())
                continue
            }

            val adjustedDepartureTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong())
            val calculateBoardingTime = calculateBoardingTime(leg, adjustedDepartureTime, TimeDirection.BEFORE)
            adjustedLegs[i] = leg.copy(departureDateTime = calculateBoardingTime.toString())
            adjustBaseTime = calculateBoardingTime
        }

        // 5. 기준점 뒤쪽 시간 재조정
        adjustBaseTime =
            LocalDateTime.parse(adjustedLegs[adjustBaseIndex].departureDateTime!!)
                .plusSeconds(adjustedLegs[adjustBaseIndex].sectionTime.toLong())
        for (i in adjustBaseIndex + 1 until adjustedLegs.size) {
            val leg = adjustedLegs[i]

            if (adjustBaseTime == null) {
                adjustedLegs[i] = leg.copy(departureDateTime = null)
                continue
            }

            if (leg.isWalk()) {
                adjustBaseTime = adjustBaseTime.plusSeconds(leg.sectionTime.toLong())
                continue
            }

            val adjustedDepartureTime = adjustBaseTime
            val calculateBoardingTime = calculateBoardingTime(leg, adjustedDepartureTime, TimeDirection.AFTER)
            adjustedLegs[i] = leg.copy(departureDateTime = calculateBoardingTime.toString())
            adjustBaseTime = calculateBoardingTime.plusSeconds(leg.sectionTime.toLong())
        }
        return adjustedLegs
    }

    private fun calculateBoardingTime(
        leg: LastRouteLeg,
        adjustedDepartureTime: LocalDateTime,
        direction: TimeDirection
    ): LocalDateTime =
        when (leg.transitInfo) {
            is TransitInfo.SubwayInfo ->
                leg.transitInfo.timeTable.findNearestTime(adjustedDepartureTime, direction)?.departureTime
                    ?: throw TransitException.of(
                        TransitError.NOT_FOUND_SPECIFIED_TIME,
                        "지하철 '${leg.route}'의 ${leg.start.name}'역에서 $adjustedDepartureTime ${direction}의 시간표를 찾을 수 없습니다."
                    )

            is TransitInfo.BusInfo -> {
                try {
                    leg.transitInfo.timeTable.calculateNearestTime(adjustedDepartureTime, direction)
                } catch (e: TransitException) {
                    throw TransitException.of(
                        TransitError.NOT_FOUND_SPECIFIED_TIME,
                        "버스 '${leg.route}'의 ${leg.start.name}'정류장에서" +
                            " $adjustedDepartureTime ${direction}의 시간표를 찾을 수 없습니다.",
                        e
                    )
                }
            }

            TransitInfo.NoInfoTable -> throw TransitException.of(
                TransitError.NOT_FOUND_SPECIFIED_TIME,
                "해당 교통수단의 막차 시간 정보가 없습니다. ${leg.mode} - ${leg.start.name} -> ${leg.end.name}"
            )
        }

    private fun calculateDepartureDateTime(legs: List<LastRouteLeg>): LocalDateTime {
        val firstTransitIndex = legs.indexOfFirst { it.isTransit() }
        val firstTransit = legs[firstTransitIndex]
        val departureDateTime = LocalDateTime.parse(firstTransit.departureDateTime!!)
        val totalWalkTime =
            if (firstTransitIndex > 0) {
                legs.subList(0, firstTransitIndex).filter { it.isWalk() }.sumOf { it.sectionTime.toLong() }
            } else {
                0
            }
        return departureDateTime.minusSeconds(totalWalkTime)
    }

    private fun calculateTotalTime(
        adjustedLegs: List<LastRouteLeg>,
        departureDateTime: LocalDateTime
    ): Long {
        val lastTransitIndex = adjustedLegs.indexOfLast { it.isTransit() }
        val lastTransit = adjustedLegs[lastTransitIndex]
        val lastTransitDepartureTime = LocalDateTime.parse(lastTransit.departureDateTime!!)
        var arrivalTime = lastTransitDepartureTime.plusSeconds(lastTransit.sectionTime.toLong())

        val totalWalkTime =
            adjustedLegs.drop(lastTransitIndex + 1)
                .filter { it.isWalk() }.sumOf { it.sectionTime.toLong() }

        arrivalTime = arrivalTime.plusSeconds(totalWalkTime)
        return Duration.between(departureDateTime, arrivalTime).seconds
    }
}
