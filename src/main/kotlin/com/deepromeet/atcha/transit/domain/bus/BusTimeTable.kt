package com.deepromeet.atcha.transit.domain.bus

import com.deepromeet.atcha.transit.domain.TimeDirection
import com.deepromeet.atcha.transit.exception.TransitError
import com.deepromeet.atcha.transit.exception.TransitException
import java.time.LocalDateTime

data class BusTimeTable(
    val firstTime: LocalDateTime,
    val lastTime: LocalDateTime,
    val term: Int
) {
    init {
        require(firstTime <= lastTime) { "첫차 시각($firstTime)이 막차 시각($lastTime)보다 늦습니다." }
    }

    fun calculateNearestTime(
        time: LocalDateTime,
        timeDirection: TimeDirection
    ): LocalDateTime {
        require(term > 0) { "잘못된 배차 간격(term=$term)입니다. 0보다 커야 합니다." }
        return when (timeDirection) {
            TimeDirection.BEFORE -> {
                if (time.isBefore(firstTime)) {
                    throw TransitException.of(
                        TransitError.NOT_FOUND_SPECIFIED_TIME,
                        "첫차 시각($firstTime) 이전에는 운행 정보가 없습니다."
                    )
                }

                var candidate = lastTime
                while (candidate.isAfter(time)) {
                    candidate = candidate.minusMinutes(term.toLong())
                }

                if (candidate.isBefore(firstTime)) {
                    throw TransitException.of(
                        TransitError.NOT_FOUND_SPECIFIED_TIME,
                        "$firstTime 이전에는 운행 정보가 없습니다."
                    )
                }
                candidate
            }

            TimeDirection.AFTER -> {
                if (time.isAfter(lastTime)) {
                    throw TransitException.of(
                        TransitError.NOT_FOUND_SPECIFIED_TIME,
                        "막차 시각($lastTime) 이후에는 운행 정보가 없습니다."
                    )
                }

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
