package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.transit.api.request.LastRoutesRequest
import com.deepromeet.atcha.transit.api.response.LastRoutesResponse
import com.deepromeet.atcha.transit.api.response.Legs
import com.deepromeet.atcha.transit.exception.TransitException
import com.deepromeet.atcha.transit.infrastructure.client.tmap.TMapTransitClient
import com.deepromeet.atcha.transit.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Itinerary
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.Leg
import com.deepromeet.atcha.transit.infrastructure.client.tmap.response.TMapRouteResponse
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
    private val lastRoutesResponseRedisTemplate: RedisTemplate<String, LastRoutesResponse>
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

    fun getLastRoutes(
        userId: Long,
        request: LastRoutesRequest
    ): List<LastRoutesResponse> {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val baseDate = today.format(dateFormatter)
        val hours = listOf("21", "22", "23")

        val allRoutes = getItineraries(hours, baseDate, request)
        val deduplicatedRoutes = filterAndDeduplicateItineraries(allRoutes)

//        printItineraryComparison(allRoutes, deduplicatedRoutes)

        // TODO : 경로 막차 계산
        val lastRoutesResponses =
            deduplicatedRoutes.map { route ->
                // 1. 경로 내 대중교통 별 막차 시간 조회
                val calculatedLegs = calculateLegLastArriveDateTimes(route.legs)
                // 2. 도보 시간 조정  - 모든 도보는 2분씩 더해준다.
                val adjustedWalkLegs = increaseWalkTime(calculatedLegs)
                // 3. 막차 시간 기준, 경로 내 대중교통 탑승 가능 여부 확인
                val adjustedLegs = adjustTransitDepartureTimes(adjustedWalkLegs)
                // 4. 출발 시간 계산
                val departureDateTime = calculateDepartureDateTime(adjustedLegs)
                // 5. 총 소요 시간 계산
                val totalTime = calculateTotalTime(adjustedLegs, departureDateTime)

                LastRoutesResponse(
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

        val now = LocalDateTime.now().minusMinutes(30) // TODO : 실제 메서드 붙이면 now()로 변경
        val filteredRoutes =
            lastRoutesResponses.filter { response ->
                val departureDateTime = LocalDateTime.parse(response.departureDateTime)
                departureDateTime.isAfter(now)
            }
//        compareFilteredRoutes(lastRoutesResponses, filteredRoutes)

        saveRoutesToRedis(filteredRoutes)

        return sortedByMinTransfer(filteredRoutes)
    }

    private fun getItineraries(
        hours: List<String>,
        baseDate: String?,
        request: LastRoutesRequest
    ) = hours.flatMap { hour ->
        val searchDttm = "$baseDate${hour}00"
        val response =
            tMapTransitClient.getRoutes(
                TMapRouteRequest(
                    startX = request.startLon,
                    startY = request.startLat,
                    endX = request.endLon,
                    endY = request.endLat,
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

        response.metaData?.plan?.itineraries ?: throw TransitException.TransitApiError
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

    private fun calculateLegLastArriveDateTimes(legs: List<Leg>): List<Legs> {
        return legs.map { leg ->
            val departureDateTime =
                if (leg.mode != "WALK") LocalDateTime.now().toString() else null // TODO : 막차 시간 계산 메서드 사용 필요
            leg.toLeg(departureDateTime)
        }
    }

    private fun increaseWalkTime(legs: List<Legs>): List<Legs> {
        return legs.map { leg ->
            if (leg.mode == "WALK") {
                leg.copy(sectionTime = leg.sectionTime + 120) // 기존 시간 + 120초
            } else {
                leg
            }
        }
    }

    private fun adjustTransitDepartureTimes(legs: List<Legs>): List<Legs> {
        val adjustedLegs = legs.toMutableList()
        val transitLegs =
            adjustedLegs.withIndex().filter { it.value.mode != "WALK" && it.value.departureDateTime != null }

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
        var adjustBaseTime = LocalDateTime.parse(adjustedLegs[adjustBaseIndex].departureDateTime!!)
        for (i in adjustBaseIndex - 1 downTo 0) {
            val leg = adjustedLegs[i]

            if (leg.mode == "WALK") {
                adjustBaseTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong())
                continue
            }

            val adjustedDepartureTime = adjustBaseTime.minusSeconds(leg.sectionTime.toLong()).toString()
            // TODO : adjustedDepartureTime 이전 대중교통을 탑승할 수 있는 출발 시간 계산 필요 (Method 사용)
            adjustedLegs[i] = leg.copy(departureDateTime = adjustedDepartureTime)
            adjustBaseTime = LocalDateTime.parse(adjustedDepartureTime)
        }

        // 5. 기준점 뒤쪽 시간 재조정
        adjustBaseTime =
            LocalDateTime.parse(adjustedLegs[adjustBaseIndex].departureDateTime!!)
                .plusSeconds(adjustedLegs[adjustBaseIndex].sectionTime.toLong())
        for (i in adjustBaseIndex + 1 until adjustedLegs.size) {
            val leg = adjustedLegs[i]

            if (leg.mode == "WALK") {
                adjustBaseTime = adjustBaseTime.plusSeconds(leg.sectionTime.toLong())
                continue
            }

            val adjustedDepartureTime = adjustBaseTime.toString()
            // TODO : adjustedDepartureTime 이후 대중교통을 탑승할 수 있는 출발 시간 계산 필요 (Method 사용)
            adjustedLegs[i] = leg.copy(departureDateTime = adjustedDepartureTime)

            adjustBaseTime = adjustBaseTime.plusSeconds(leg.sectionTime.toLong())
        }
        return adjustedLegs
    }

    private fun calculateDepartureDateTime(legs: List<Legs>): LocalDateTime {
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
        adjustedLegs: List<Legs>,
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

    private fun Leg.toLeg(departureDateTime: String?): Legs {
        return Legs(
            distance = this.distance,
            sectionTime = this.sectionTime,
            mode = this.mode,
            departureDateTime = departureDateTime,
            route = this.route,
            type = this.type.toString(),
            service = this.service.toString(),
            start = this.start,
            end = this.end,
            passStopList = this.passStopList?.stations,
            step = this.steps,
            passShape = this.passShape?.linestring
        )
    }

    // 정렬 : 환승-시간 순으로 정렬로 고정
    private fun sortedByMinTransfer(routes: List<LastRoutesResponse>) =
        routes.sortedWith(
            compareBy({ it.transferCount }, { it.totalTime })
        )

//    private fun sortedByMinWalking(routes: List<LastRoutesResponse>) = routes.sortedWith(
//        compareBy({ it.totalWalkDistance }, { it.totalTime })
//    )

//    private fun sortedByShortestTime(routes: List<LastRoutesResponse>) = routes.sortedWith(
//        compareBy({ it.totalTime }, { it.transferCount })
//    )

    private fun saveRoutesToRedis(routes: List<LastRoutesResponse>) {
        routes.forEach { route ->
            val key = "routes:last:${route.routeId}"
            lastRoutesResponseRedisTemplate.opsForValue().set(key, route, Duration.ofHours(12))
        }
    }

    // 중복 제거 전후 확인 코드 - 데이터 확인용
    fun printItineraryComparison(
        original: List<Itinerary>,
        deduplicated: List<Itinerary>
    ) {
        println("=== 제거 전 Itinerary ===")
        println(original.size)
        original.forEach { itinerary ->
            itinerary.legs.forEach { leg ->
                println("Start: ${leg.start.name}, End: ${leg.end.name}, Route: ${leg.route}")
            }
            println("-----")
        }

        println("\n=== 중복 제거 후 Itinerary ===")
        println(deduplicated.size)
        deduplicated.forEach { itinerary ->
            itinerary.legs.forEach { leg ->
                println("Start: ${leg.start.name}, End: ${leg.end.name}, Route: ${leg.route}")
            }
            println("-----")
        }
    }

    // 이전 시간 필터 전후 확인 코드 - 데이터 확인용
    fun compareFilteredRoutes(
        lastRoutesResponses: List<LastRoutesResponse>,
        filteredRoutes: List<LastRoutesResponse>
    ) {
        println("\n🚀 필터 전/후 경로 비교 🚀\n")

        lastRoutesResponses.forEach { original ->
            val filtered = filteredRoutes.find { it.departureDateTime == original.departureDateTime }

            val originalLegsInfo =
                original.legs.joinToString { leg ->
                    "(Start: ${leg.start.name}, End: ${leg.end.name}, Route: ${leg.route ?: "N/A"})"
                }

            if (filtered != null) {
                val filteredLegsInfo =
                    filtered.legs.joinToString { leg ->
                        "(Start: ${leg.start.name}, End: ${leg.end.name}, Route: ${leg.route ?: "N/A"})"
                    }
                println("🔹 Before: ${original.departureDateTime} | Legs: $originalLegsInfo")
                println("🔹 After : ${filtered.departureDateTime} | Legs: $filteredLegsInfo\n")
            } else {
                println("🔹 Before: ${original.departureDateTime} | Legs: $originalLegsInfo")
                println("🔹 After : (제거됨)\n")
            }
        }
    }
}
