package com.deepromeet.atcha.route.domain

import com.deepromeet.atcha.route.exception.RouteError
import com.deepromeet.atcha.route.exception.RouteException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.absoluteValue

data class LastRoute(
    val id: String,
    val departureDateTime: String,
    val totalTime: Int,
    val totalWalkTime: Int,
    val totalWalkDistance: Int,
    val transferCount: Int,
    val totalDistance: Int,
    val pathType: Int,
    val legs: List<LastRouteLeg>
) {
    fun parseDepartureTime(): LocalDateTime {
        return LocalDateTime.parse(departureDateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
    }

    fun calculateRemainingTime(): Int {
        return Duration.between(
            LocalDateTime.now(),
            LocalDateTime.parse(departureDateTime)
        ).toSeconds().toInt().absoluteValue
    }

    fun findFirstTransit(): LastRouteLeg {
        return legs.first { it.isTransit() }
    }

    fun findFirstBus(): LastRouteLeg {
        return legs.firstOrNull { it.isBus() } ?: throw RouteException.of(
            RouteError.INVALID_LAST_ROUTE,
            "$id 경로에서 첫 버스를 찾을 수 없습니다."
        )
    }

    fun calcWalkingTimeToFirstTransit(): Long =
        legs.takeWhile { !it.isTransit() }
            .sumOf { it.sectionTime }
            .toLong()

    companion object {
        fun create(
            itinerary: RouteItinerary,
            adjustedLegs: List<LastRouteLeg>
        ): LastRoute {
            val departureDateTime =
                calculateDepartureTime(adjustedLegs)
                    .validateLastRouteDeparture()
            val arrivalTime = calculateArrivalTime(adjustedLegs)
            val totalTime = Duration.between(departureDateTime, arrivalTime).seconds

            return LastRoute(
                id = UUID.randomUUID().toString(),
                departureDateTime =
                    departureDateTime
                        .truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                totalTime = totalTime.toInt(),
                totalWalkTime = itinerary.totalWalkTime,
                totalWalkDistance = itinerary.totalWalkDistance,
                transferCount = itinerary.transferCount,
                totalDistance = itinerary.totalDistance,
                pathType = itinerary.pathType,
                legs = adjustedLegs
            )
        }

        private fun calculateDepartureTime(legs: List<LastRouteLeg>): LocalDateTime {
            val firstTransitIndex = legs.indexOfFirst { it.isTransit() }
            val firstTransitLeg = legs[firstTransitIndex]
            val initialWalkTime =
                legs.take(firstTransitIndex)
                    .sumOf { it.sectionTime }
                    .toLong()

            return LocalDateTime.parse(firstTransitLeg.departureDateTime!!)
                .minusSeconds(initialWalkTime)
        }

        private fun calculateArrivalTime(legs: List<LastRouteLeg>): LocalDateTime {
            val lastTransitIndex = legs.indexOfLast { it.isTransit() }
            val lastTransitLeg = legs[lastTransitIndex]
            val lastTransitArrivalTime =
                LocalDateTime.parse(lastTransitLeg.departureDateTime!!)
                    .plusSeconds(lastTransitLeg.sectionTime.toLong())

            val finalWalkTime =
                legs.drop(lastTransitIndex + 1)
                    .sumOf { it.sectionTime }
                    .toLong()

            return lastTransitArrivalTime.plusSeconds(finalWalkTime)
        }
    }
}
