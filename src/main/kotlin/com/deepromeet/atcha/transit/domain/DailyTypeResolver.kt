package com.deepromeet.atcha.transit.domain

import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate

@Component
class DailyTypeResolver(
    private val holidayFetcher: HolidayFetcher
) {
    fun resolve(date: LocalDate = LocalDate.now()): DailyType {
        return when {
            isHoliday(date) -> DailyType.HOLIDAY
            date.dayOfWeek == DayOfWeek.SATURDAY -> DailyType.HOLIDAY
            date.dayOfWeek == DayOfWeek.SUNDAY -> DailyType.HOLIDAY
            else -> DailyType.WEEKDAY
        }
    }

    private fun isHoliday(date: LocalDate): Boolean {
        val holidays = holidayFetcher.fetch(date.year)
        return holidays.contains(date)
    }
}
