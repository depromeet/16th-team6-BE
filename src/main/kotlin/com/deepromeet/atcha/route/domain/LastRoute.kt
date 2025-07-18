package com.deepromeet.atcha.route.domain

import java.time.Duration
import java.time.LocalDateTime
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
    fun calculateRemainingTime(): Int {
        return Duration.between(
            LocalDateTime.now(),
            LocalDateTime.parse(departureDateTime)
        ).toSeconds().toInt().absoluteValue
    }

    fun findFirstTransit(): LastRouteLeg {
        return legs.first { it.isTransit() }
    }

    fun findFirstBus(): LastRouteLeg? {
        return legs.firstOrNull { it.isBus() }
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
            val increasedWalkTimeLegs = adjustedLegs.withIncreasedWalkTime()
            val departureDateTime = calculateDepartureTime(adjustedLegs)
            val arrivalTime = calculateArrivalTime(adjustedLegs)
            val totalTime = Duration.between(departureDateTime, arrivalTime).seconds

            return LastRoute(
                id = UUID.randomUUID().toString(),
                departureDateTime = departureDateTime.toString(),
                totalTime = totalTime.toInt(),
                totalWalkTime = itinerary.totalWalkTime,
                totalWalkDistance = itinerary.totalWalkDistance,
                transferCount = itinerary.transferCount,
                totalDistance = itinerary.totalDistance,
                pathType = itinerary.pathType,
                legs = increasedWalkTimeLegs
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

        private fun List<LastRouteLeg>.withIncreasedWalkTime(): List<LastRouteLeg> {
            return this.mapIndexed { index, currentLeg ->
                val nextLeg = this.getOrNull(index + 1)
                currentLeg.withIncreasedWalkTime(nextLeg)
            }
        }
    }
}
