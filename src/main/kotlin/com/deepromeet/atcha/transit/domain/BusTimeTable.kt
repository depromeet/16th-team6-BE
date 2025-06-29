package com.deepromeet.atcha.transit.domain

import java.time.LocalDateTime

data class BusTimeTable(
    val firstTime: LocalDateTime,
    val lastTime: LocalDateTime,
    val term: Int
) {
    fun calculateNearestTime(
        time: LocalDateTime,
        timeDirection: TimeDirection
    ): LocalDateTime? {
        return when (timeDirection) {
            TimeDirection.BEFORE -> {
                if (time.isBefore(firstTime)) return null

                var candidate = lastTime
                while (candidate.isAfter(time)) {
                    candidate = candidate.minusMinutes(term.toLong())
                }

                if (candidate.isBefore(firstTime)) null else candidate
            }
            TimeDirection.AFTER -> {
                if (time.isAfter(lastTime)) return null
                if (time.isBefore(firstTime)) return firstTime

                var candidate = firstTime
                while (candidate.isBefore(time)) {
                    candidate = candidate.plusMinutes(term.toLong())
                }
                candidate
            }
        }
    }
}
