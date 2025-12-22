package com.deepromeet.atcha.transit.domain.subway

import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.abs

data class SubwayRealTimeArrivals(
    val arrivals: List<SubwayArrival>
) {
    fun getClosestArrivals(targetDepartureTime: LocalDateTime): List<SubwayArrival>? {
        if (arrivals.isEmpty()) return null

        val sortedArrivals = arrivals.sortedBy { it.remainingTimeSeconds }

        val targetIndex =
            sortedArrivals.withIndex().minByOrNull { (_, arrival) ->
                abs(
                    Duration.between(targetDepartureTime, arrival.expectedArrivalTime)
                        .seconds
                )
            }?.index ?: return null

        return buildList {
            add(sortedArrivals[targetIndex])
            if (targetIndex + 1 < sortedArrivals.size) {
                add(sortedArrivals[targetIndex + 1])
            }
        }
    }

    companion object {
        fun empty() = SubwayRealTimeArrivals(emptyList())
    }
}
