package com.deepromeet.atcha.transit.domain.subway

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class SubwayTime(
    val time: LocalTime,
    val dayScope: DayScope
) {
    fun toLocalDateTime(referenceDate: LocalDate = LocalDate.now()): LocalDateTime {
        val targetDate =
            when (dayScope) {
                DayScope.TODAY -> referenceDate
                DayScope.TOMORROW -> referenceDate.plusDays(1)
            }
        return LocalDateTime.of(targetDate, time)
    }
}

enum class DayScope {
    TODAY,
    TOMORROW
}
