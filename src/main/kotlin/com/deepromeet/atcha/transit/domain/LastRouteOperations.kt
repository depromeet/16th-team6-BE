package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.TMapTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Leg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class LastRouteOperations(
    private val tMapTransitClient: TMapTransitClient,
    private val subwayManager: SubwayManager,
    private val busManager: BusManager
) {
    fun getItineraries(
        start: Coordinate,
        end: Coordinate
    ): List<Itinerary> {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val baseDate = today.format(dateFormatter) + "2300"

        val response =
            tMapTransitClient.getRoutes(
                TMapRouteRequest(
                    startX = start.lon.toString(),
                    startY = start.lat.toString(),
                    endX = end.lon.toString(),
                    endY = end.lat.toString(),
                    count = 10,
                    searchDttm = baseDate
                )
            )

        response.result?.let { result ->
            when (result.status) {
                11 -> throw TransitException.DistanceTooShort
                else -> throw TransitException.ServiceAreaNotSupported
            }
        }

        return response.metaData?.plan?.itineraries ?: throw TransitException.TransitApiError
    }

    fun filterAndDeduplicateItineraries(itineraries: List<Itinerary>): List<Itinerary> {
        return itineraries.filterNot { itinerary ->
            val transitModes = itinerary.legs.filter { it.mode == "SUBWAY" || it.mode == "BUS" }
            val busCountExcludingFirst = transitModes.drop(1).count { it.mode == "BUS" }
            val hasTrain = itinerary.legs.any { it.mode == "TRAIN" }
            val hasExpressSubway =
                itinerary.legs.any {
                    it.mode == "SUBWAY" && it.route != null && it.route.contains("(급행)")
                }
            val hasValidModes =
                itinerary.legs.any {
                    it.mode == "WALK" ||
                        (it.mode == "SUBWAY" && it.route != null && !it.route.contains("(급행)")) ||
                        it.mode == "BUS"
                }
            hasTrain || hasExpressSubway || !hasValidModes ||
                (transitModes.size >= 3 && busCountExcludingFirst >= 2) || transitModes.size >= 5
        }.associateBy { itinerary ->
            itinerary.legs.joinToString("|") { leg ->
                "${leg.start.name}-${leg.end.name}-${leg.route ?: ""}"
            }
        }.values.toList()
    }

    suspend fun calculateRoute(route: Itinerary): LastRoutes? {
        // 1. 경로 내 대중교통 별 막차 시간 조회
        val calculatedLegs = calculateLegLastArriveDateTimes(route.legs) ?: return null
        // 2. 도보 시간 조정  - 모든 도보는 2분씩 더해준다.
        val adjustedWalkLegs = increaseWalkTime(calculatedLegs)
        // 3. 막차 시간 기준, 경로 내 대중교통 탑승 가능 여부 확인
        val adjustedLegs = adjustTransitDepartureTimes(adjustedWalkLegs)
        // 4. 유효하지 않는 경로 제거 (leg.departureTime=null 인 경우)
        if (adjustedLegs.any { leg ->
                (leg.mode == "SUBWAY" || leg.mode == "BUS") &&
                    (leg.departureDateTime == null || leg.departureDateTime == "null")
            }
        ) {
            return null
        }
        // 5. 출발 시간 계산
        val departureDateTime = calculateDepartureDateTime(adjustedLegs)
        // 6. 총 소요 시간 계산
        val totalTime = calculateTotalTime(adjustedLegs, departureDateTime)

        return LastRoutes(
            routeId = UUID.randomUUID().toString(),
            departureDateTime = departureDateTime.toString(),
            totalTime = totalTime.toInt(),
            totalWalkTime = route.totalWalkTime,
            transferCount = route.transferCount,
            totalWorkDistance = route.totalWalkDistance,
            totalDistance = route.totalDistance,
            pathType = route.pathType,
            legs = adjustedLegs
        )
    }

    private suspend fun calculateLegLastArriveDateTimes(legs: List<Leg>): List<LastRouteLeg>? {
        val calculatedLegs =
            coroutineScope {
                legs.map { leg ->
                    async(Dispatchers.IO) {
                        val departureDateTime =
                            when (leg.mode) {
                                "SUBWAY" ->
                                    getLastTime(
                                        SubwayLine.fromRouteName(leg.route!!),
                                        leg.start.name,
                                        leg.end.name
                                    )?.departureTime?.toString()

                                "BUS" ->
                                    busManager.getArrivalInfo(
                                        leg.route!!.split(":")[1],
                                        BusStationMeta(
                                            leg.start.name.removeSuffix(),
                                            Coordinate(leg.start.lat, leg.start.lon)
                                        )
                                    )?.lastTime?.toString()

                                else -> null
                            }
                        leg.toLeg(departureDateTime)
                    }
                }.awaitAll().toList()
            }

        // null이 섞여있는 leg가 하나라도 있다면, 전체 경로 자체를 제거
        return if (calculatedLegs.any {
                (it.mode == "BUS" || it.mode == "SUBWAY") &&
                    (it.departureDateTime == null || it.departureDateTime == "null")
            }
        ) {
            null
        } else {
            calculatedLegs
        }
    }

    private fun getLastTime(
        subwayLine: SubwayLine,
        startStationName: String,
        endStationName: String
    ): SubwayTime? {
        val routes = subwayManager.getRoutes(subwayLine)
        val startStation = subwayManager.getStation(subwayLine, startStationName)
        val endStation = subwayManager.getStation(subwayLine, endStationName)
        return subwayManager.getTimeTable(startStation, endStation, routes)?.getLastTime(endStation, routes)
    }

    private fun increaseWalkTime(legs: List<LastRouteLeg>): List<LastRouteLeg> {
        return legs.mapIndexed { index, currentLeg ->
            val nextLeg = legs.getOrNull(index + 1)
            if (currentLeg.mode == "WALK" && (nextLeg?.mode == "SUBWAY" || nextLeg?.mode == "BUS")) {
                currentLeg.copy(sectionTime = currentLeg.sectionTime + 120)
            } else {
                currentLeg
            }
        }
    }

    private fun adjustTransitDepartureTimes(legs: List<LastRouteLeg>): List<LastRouteLeg> {
        val adjustedLegs = legs.toMutableList()
        if (adjustedLegs.any { it.mode != "WALK" && it.departureDateTime == null }) return adjustedLegs

        val transitLegs = adjustedLegs.withIndex().filter { it.value.mode != "WALK" }

        if (transitLegs.isEmpty()) return adjustedLegs

        // 1. 대중교통 기준 가장 빠른 막차 시간 찾기
        val earliestTransitLeg = transitLegs.minBy { LocalDateTime.parse(it.value.departureDateTime!!) }

        var isAllRideable = true
        var lastUnrideableIndex: Int? = null

        // 2. 가장 빠른 출발 시간을 기준으로 뒤에 있는 대중교통 탑승 가능 여부 확인
        for (i in earliestTransitLeg.index until adjustedLegs.lastIndex) {
            val currentLeg = adjustedLegs[i]
            if (currentLeg.mode == "WALK") continue

            // 2-1. 출발 시간 + 소요 시간
            var currentLegAvailableTime =
                LocalDateTime.parse(currentLeg.departureDateTime!!).plusSeconds(currentLeg.sectionTime.toLong())

            // 2-2. 2-1 결과 시간과 다음 대중교통 출발 시간 비교 -> 탑승 가능 여부 확인
            var nextIndex = i + 1
            while (nextIndex <= adjustedLegs.lastIndex && adjustedLegs[nextIndex].mode == "WALK") {
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

        // 3. 기준점 설정 : 가장 빠른 출발 시간 기준 or 탑승 불가한 마지막 대중교통
        val adjustBaseIndex = if (isAllRideable) earliestTransitLeg.index else lastUnrideableIndex!!

        // 4. 기준점 앞쪽 시간 재조정
        var adjustBaseTime = adjustedLegs[adjustBaseIndex].departureDateTime?.let { LocalDateTime.parse(it) }
        for (i in adjustBaseIndex - 1 downTo 0) {
            val leg = adjustedLegs[i]

            if (adjustBaseTime == null) {
                adjustedLegs[i] = leg.copy(departureDateTime = null)
                continue
            }

            if (leg.mode == "WALK") {
                adjustBaseTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong())
                continue
            }

            val adjustedDepartureTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong())
            val calculateBoardingTime = calculateBoardingTime(leg, adjustedDepartureTime, TimeDirection.BEFORE)
            adjustedLegs[i] = leg.copy(departureDateTime = calculateBoardingTime?.toString())
            adjustBaseTime = calculateBoardingTime?.let { LocalDateTime.parse(it.toString()) }
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

            if (leg.mode == "WALK") {
                adjustBaseTime = adjustBaseTime.plusSeconds(leg.sectionTime.toLong())
                continue
            }

            val adjustedDepartureTime = adjustBaseTime
            val calculateBoardingTime = calculateBoardingTime(leg, adjustedDepartureTime, TimeDirection.AFTER)
            adjustedLegs[i] = leg.copy(departureDateTime = calculateBoardingTime.toString())
            adjustBaseTime = calculateBoardingTime?.plusSeconds(leg.sectionTime.toLong())
        }
        return adjustedLegs
    }

    fun calculateBoardingTime(
        leg: LastRouteLeg,
        adjustedDepartureTime: LocalDateTime,
        direction: TimeDirection
    ): LocalDateTime? =
        when (leg.mode) {
            "SUBWAY" -> {
                val subwayLine = SubwayLine.fromRouteName(leg.route!!)
                val routes = subwayManager.getRoutes(subwayLine)
                val startStation = subwayManager.getStation(subwayLine, leg.start.name)
                val endStation = subwayManager.getStation(subwayLine, leg.end.name)

                subwayManager.getTimeTable(startStation, endStation, routes)
                    ?.findNearestTime(adjustedDepartureTime, direction)?.departureTime
            }

            "BUS" ->
                busManager.getArrivalInfo(
                    leg.route!!.split(":")[1],
                    BusStationMeta(
                        leg.start.name.removeSuffix(),
                        Coordinate(leg.start.lat, leg.start.lon)
                    )
                )?.getNearestTime(adjustedDepartureTime, direction)

            else -> null
        }

    fun calculateDepartureDateTime(legs: List<LastRouteLeg>): LocalDateTime {
        val firstTransitIndex = legs.indexOfFirst { it.mode != "WALK" }
        val firstTransit = legs[firstTransitIndex]
        val departureDateTime = LocalDateTime.parse(firstTransit.departureDateTime!!)
        val totalWalkTime =
            if (firstTransitIndex > 0) {
                legs.subList(0, firstTransitIndex).filter { it.mode == "WALK" }.sumOf { it.sectionTime.toLong() }
            } else {
                0
            }

        return departureDateTime.minusSeconds(totalWalkTime)
    }

    private fun calculateTotalTime(
        adjustedLegs: List<LastRouteLeg>,
        departureDateTime: LocalDateTime
    ): Long {
        val lastTransitIndex = adjustedLegs.indexOfLast { it.mode != "WALK" }
        val lastTransit = adjustedLegs[lastTransitIndex]

        val lastTransitDepartureTime = LocalDateTime.parse(lastTransit.departureDateTime!!)
        var arrivalTime = lastTransitDepartureTime.plusSeconds(lastTransit.sectionTime.toLong())

        val totalWalkTime =
            adjustedLegs.drop(lastTransitIndex + 1) // 마지막 대중교통 이후 구간
                .filter { it.mode == "WALK" }.sumOf { it.sectionTime.toLong() }

        arrivalTime = arrivalTime.plusSeconds(totalWalkTime)

        // 4. 총 소요 시간 계산 (초 단위)
        return Duration.between(departureDateTime, arrivalTime).seconds
    }

    private fun Leg.toLeg(departureDateTime: String?): LastRouteLeg {
        return LastRouteLeg(
            distance = this.distance,
            sectionTime = this.sectionTime,
            mode = this.mode,
            departureDateTime = departureDateTime,
            route = this.route,
            type = this.type.toString(),
            service = this.service.toString(),
            start = this.start,
            end = this.end,
            passStopList = this.passStopList?.stationList,
            step = this.steps,
            passShape = this.passShape?.linestring
        )
    }

    private fun String.removeSuffix(): String = this.replace("(중)", "").trim()

    fun getFilteredRoutes(lastRoutes: List<LastRoutes>): List<LastRoutes> {
        val now = LocalDateTime.now()
        return lastRoutes.filter { response ->
            val departureDateTime = LocalDateTime.parse(response.departureDateTime)
            departureDateTime.isAfter(now)
        }
    }

    fun sort(
        sortType: LastRouteSortType,
        routes: List<LastRoutes>
    ): List<LastRoutes> {
        return when (sortType) {
            LastRouteSortType.MINIMUM_TRANSFERS -> sortedByMinTransfer(routes)
            LastRouteSortType.DEPARTURE_TIME_DESC -> sortedByDepartureTimeDesc(routes)
        }
    }

    private fun sortedByMinTransfer(routes: List<LastRoutes>) =
        routes.sortedWith(
            compareBy({ it.transferCount }, { it.totalTime })
        )

    private fun sortedByDepartureTimeDesc(routes: List<LastRoutes>) = routes.sortedByDescending { it.departureDateTime }
}
