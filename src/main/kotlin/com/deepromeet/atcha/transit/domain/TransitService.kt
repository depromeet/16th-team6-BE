package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.api.response.LastRouteLeg
import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.TMapTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Leg
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.TMapRouteResponse
import com.deepromeet.atcha.user.domain.UserReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class TransitService(
    private val tMapTransitClient: TMapTransitClient,
    private val taxiFareFetcher: TaxiFareFetcher,
    private val busManager: BusManager,
    private val subwayManager: SubwayManager,
    private val subwayStationBatchAppender: SubwayStationBatchAppender,
    private val lastRoutesResponseRedisTemplate: RedisTemplate<String, LastRoutesResponse>,
    private val regionIdentifier: RegionIdentifier,
    private val userReader: UserReader
) {
    fun init() {
        subwayStationBatchAppender.appendAll()
    }

    fun getRoutes(): TMapRouteResponse {
        return tMapTransitClient.getRoutes(
            TMapRouteRequest(
                startX = "126.978388",
                startY = "37.566610",
                endX = "127.027636",
                endY = "37.497950",
                count = 5,
                searchDttm = "202502142100"
            )
        )
    }

    fun getBusArrivalInfo(
        routeName: String,
        stationName: String,
        coordinate: Coordinate
    ): BusArrival {
        return busManager.getArrivalInfo(routeName, BusStationMeta(stationName, coordinate))
    }

    fun getTaxiFare(
        start: Coordinate,
        end: Coordinate
    ): Fare {
        return taxiFareFetcher.fetch(start, end) ?: throw TransitException.TaxiFareFetchFailed
    }

    fun getLastTime(
        subwayLine: SubwayLine,
        startStationName: String,
        endStationName: String
    ): SubwayTime? {
        val routes = subwayManager.getRoutes(subwayLine)
        val startStation = subwayManager.getStation(subwayLine, startStationName)
        val endStation = subwayManager.getStation(subwayLine, endStationName)
        return subwayManager.getTimeTable(startStation, endStation, routes).getLastTime(endStation, routes)
    }

    suspend fun getLastRoutes(
        userId: Long,
        start: Coordinate,
        endLat: String?,
        endLon: String?
    ): List<LastRoutesResponse> {
        val end =
            if (endLat == null || endLon == null) {
                Coordinate(
                    userReader.read(userId).address.lat,
                    userReader.read(userId).address.lon
                )
            } else {
                Coordinate(endLat.toDouble(), endLon.toDouble())
            }

        // 서비스 지역인지 판별 -> 서비스 지역이 아니면 Exception 발생
        regionIdentifier.identify(start)
        regionIdentifier.identify(end)

        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val baseDate = today.format(dateFormatter)

        val allRoutes =
            coroutineScope {
                listOf("21", "22", "23")
                    .map { hour ->
                        async(Dispatchers.IO) {
                            getItineraries(hour, baseDate, start, end)
                        }
                    }
                    .awaitAll()
                    .flatten()
            }

        val deduplicatedRoutes = filterAndDeduplicateItineraries(allRoutes)

        // 경로 막차 계산
        val lastRoutesResponses =
            coroutineScope {
                deduplicatedRoutes
                    .map { route ->
                        async(Dispatchers.Default) {
                            calculateRoute(route)
                        }
                    }
                    .awaitAll()
                    .filterNotNull()
            }

        val now = LocalDateTime.now()
        val filteredRoutes =
            lastRoutesResponses.filter { response ->
                val departureDateTime = LocalDateTime.parse(response.departureDateTime)
                departureDateTime.isAfter(now)
            }

        saveRoutesToRedis(filteredRoutes)

        return sortedByMinTransfer(filteredRoutes)
    }

    private suspend fun getItineraries(
        hour: String,
        baseDate: String?,
        start: Coordinate,
        end: Coordinate
    ): List<Itinerary> {
        val searchDttm = "$baseDate${hour}00"
        val response =
            tMapTransitClient.getRoutes(
                TMapRouteRequest(
                    startX = start.lon.toString(),
                    startY = start.lat.toString(),
                    endX = end.lon.toString(),
                    endY = end.lat.toString(),
                    count = 20,
                    searchDttm = searchDttm
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

    private fun filterAndDeduplicateItineraries(itineraries: List<Itinerary>): List<Itinerary> {
        return itineraries.filterNot { itinerary ->
            val transitModes = itinerary.legs.filter { it.mode == "SUBWAY" || it.mode == "BUS" }
            val busCountExcludingFirst = transitModes.drop(1).count { it.mode == "BUS" }
            val hasValidModes = itinerary.legs.any { it.mode == "WALK" || it.mode == "SUBWAY" || it.mode == "BUS" }
            !hasValidModes || (transitModes.size >= 3 && busCountExcludingFirst >= 2) || transitModes.size >= 4
        }.associateBy { itinerary ->
            itinerary.legs.joinToString("|") { leg ->
                "${leg.start.name}-${leg.end.name}-${leg.route ?: ""}"
            }
        }.values.toList()
    }

    private suspend fun calculateRoute(route: Itinerary): LastRoutesResponse? {
        // 1. 경로 내 대중교통 별 막차 시간 조회
        val calculatedLegs = calculateLegLastArriveDateTimes(route.legs)
        // 2. 도보 시간 조정  - 모든 도보는 2분씩 더해준다.
        val adjustedWalkLegs = increaseWalkTime(calculatedLegs)
        // 3. 막차 시간 기준, 경로 내 대중교통 탑승 가능 여부 확인
        val adjustedLegs = adjustTransitDepartureTimes(adjustedWalkLegs)
        // 4. 유효하지 않는 경로 제거 (leg.departureTime=null 인 경우)
        if (adjustedLegs.any { leg ->
                (leg.mode == "SUBWAY" || leg.mode == "BUS") && leg.departureDateTime == null
            }
        ) {
            return null
        }
        // 5. 출발 시간 계산
        val departureDateTime = calculateDepartureDateTime(adjustedLegs)
        // 6. 총 소요 시간 계산
        val totalTime = calculateTotalTime(adjustedLegs, departureDateTime)

        return LastRoutesResponse(
            routeId = UUID.randomUUID().toString(),
            departureDateTime = departureDateTime.toString(),
            totalTime = totalTime.toInt(),
            totalWalkTime = route.totalWalkTime,
            transferCount = route.transferCount,
            totalDistance = route.totalDistance,
            pathType = 0,
            legs = adjustedLegs
        )
    }

    private fun calculateLegLastArriveDateTimes(legs: List<Leg>): List<LastRouteLeg> {
        return legs.map { leg ->
            val departureDateTime =
                when (leg.mode) {
                    "SUBWAY" ->
                        getLastTime(
                            SubwayLine.fromRouteName(leg.route!!),
                            leg.start.name,
                            leg.end.name
                        )?.departureTime.toString()

                    "BUS" ->
                        getBusArrivalInfo(
                            leg.route!!.split(":")[1],
                            leg.start.name,
                            Coordinate(leg.start.lat, leg.start.lon)
                        ).lastTime.toString()

                    else -> null
                }
            leg.toLeg(departureDateTime)
        }
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
        var adjustBaseTime = LocalDateTime.parse(adjustedLegs[adjustBaseIndex].departureDateTime!!) ?: null
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
            adjustBaseTime =
                if (calculateBoardingTime == null) null else LocalDateTime.parse(calculateBoardingTime.toString())
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
                    .findNearestTime(adjustedDepartureTime, direction).departureTime
            }

            "BUS" ->
                getBusArrivalInfo(
                    leg.route!!.split(":")[1],
                    leg.start.name,
                    Coordinate(leg.start.lat, leg.start.lon)
                ).getNearestTime(adjustedDepartureTime, direction)

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

    // 정렬 : 환승-시간 순으로 정렬로 고정
    private fun sortedByMinTransfer(routes: List<LastRoutesResponse>) =
        routes.sortedWith(
            compareBy({ it.transferCount }, { it.totalTime })
        )

    private fun saveRoutesToRedis(routes: List<LastRoutesResponse>) {
        routes.forEach { route ->
            val key = "routes:last:${route.routeId}"
            lastRoutesResponseRedisTemplate.opsForValue().set(key, route, Duration.ofHours(12))
        }
    }
}
