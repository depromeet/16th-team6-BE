package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.transit.domain.TransitInfo
import com.deepromeet.atcha.transit.domain.bus.BusStationMeta
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.absoluteValue

data class LastRoute(
    val id: String,
    val departureDateTime: String,
    val totalTime: Int,
    val totalWalkTime: Int,
    val totalWorkDistance: Int,
    val transferCount: Int,
    val totalDistance: Int,
    val pathType: Int,
    val legs: List<LastRouteLeg>
) {
    fun calculateRemainingTime(): Int {
        return Duration.between(
            LocalDateTime.parse(departureDateTime),
            LocalDateTime.now()
        ).toSeconds().toInt().absoluteValue
    }

    fun findFirstTransit(): LastRouteLeg {
        return legs.first { it.isTransit() }
    }

    fun findFirstBus(): LastRouteLeg {
        return legs.first { it.mode == RouteMode.BUS }
    }

    fun calcWalkingTimeBeforeLeg(targetLeg: LastRouteLeg): Long =
        legs.takeWhile { it != targetLeg }
            .filter { it.mode == RouteMode.WALK }
            .sumOf { it.sectionTime }
            .toLong()

    // 새로 추가: 전체 경로의 유효성 검증
    fun isValid(): Boolean {
        return legs.filter { it.isTransit() }
            .all { it.hasDepartureTime() }
    }

    // 새로 추가: 출발 시간 계산을 도메인 객체로 이동
    fun calculateActualDepartureTime(): LocalDateTime {
        val firstTransitIndex = legs.indexOfFirst { it.isTransit() }
        if (firstTransitIndex == -1) return LocalDateTime.parse(departureDateTime)

        val firstTransit = legs[firstTransitIndex]
        val transitDepartureTime = LocalDateTime.parse(firstTransit.departureDateTime!!)
        val walkTimeBeforeTransit =
            legs.take(firstTransitIndex)
                .filter { it.isWalk() }
                .sumOf { it.sectionTime.toLong() }

        return transitDepartureTime.minusSeconds(walkTimeBeforeTransit)
    }

    // 새로 추가: 총 소요시간 계산
    fun calculateActualTotalTime(): Duration {
        val departure = calculateActualDepartureTime()
        val lastTransitIndex = legs.indexOfLast { it.isTransit() }
        if (lastTransitIndex == -1) return Duration.ZERO

        val lastTransit = legs[lastTransitIndex]
        val lastTransitDeparture = LocalDateTime.parse(lastTransit.departureDateTime!!)
        var arrival = lastTransitDeparture.plusSeconds(lastTransit.sectionTime.toLong())

        // 마지막 대중교통 이후 도보 시간 추가
        val walkTimeAfterTransit =
            legs.drop(lastTransitIndex + 1)
                .filter { it.isWalk() }
                .sumOf { it.sectionTime.toLong() }
        arrival = arrival.plusSeconds(walkTimeAfterTransit)

        return Duration.between(departure, arrival)
    }

    companion object {
        fun from(
            itinerary: RouteItinerary,
            legs: List<LastRouteLeg>
        ): LastRoute {
            val route =
                LastRoute(
                    id = UUID.randomUUID().toString(),
                    departureDateTime = "",
                    totalTime = 0,
                    totalWalkTime = itinerary.totalWalkTime,
                    totalWorkDistance = itinerary.totalWalkDistance,
                    transferCount = itinerary.transferCount,
                    totalDistance = itinerary.totalDistance,
                    pathType = itinerary.pathType,
                    legs = legs
                )

            val actualDeparture = route.calculateActualDepartureTime()
            val actualTotalTime = route.calculateActualTotalTime()

            return route.copy(
                departureDateTime = actualDeparture.toString(),
                totalTime = actualTotalTime.seconds.toInt()
            )
        }
    }
}

data class LastRouteLeg(
    val distance: Int,
    val sectionTime: Int,
    val mode: RouteMode,
    val departureDateTime: String? = null,
    val route: String? = null,
    val type: String? = null,
    val service: String? = null,
    val start: RouteLocation,
    val end: RouteLocation,
    val steps: List<RouteStep>?,
    val passStops: RoutePassStops?,
    val pathCoordinates: String?,
    val transitInfo: TransitInfo
) {
    fun isTransit(): Boolean = mode.isTransit()

    fun isWalk(): Boolean = mode.isWalk()

    fun isBus(): Boolean = mode == RouteMode.BUS

    fun resolveRouteName(): String {
        return route?.split(":")?.getOrNull(1) ?: route ?: ""
    }

    fun toBusStationMeta(): BusStationMeta {
        return BusStationMeta(
            start.name,
            start.coordinate
        )
    }

    fun hasDepartureTime(): Boolean = !departureDateTime.isNullOrBlank()
}

fun List<LastRoute>.sort(sortType: LastRouteSortType): List<LastRoute> {
    val now = LocalDateTime.now()
    val upcomingRoutes =
        this.filter {
            LocalDateTime.parse(it.departureDateTime).isAfter(now)
        }

    return when (sortType) {
        LastRouteSortType.MINIMUM_TRANSFERS ->
            upcomingRoutes.sortedWith(
                compareBy({ it.transferCount }, { it.totalTime })
            )
        LastRouteSortType.DEPARTURE_TIME_DESC -> upcomingRoutes.sortedByDescending { it.departureDateTime }
    }
}
