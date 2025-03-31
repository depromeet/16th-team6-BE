package com.deepromeet.atcha.transit.domain

import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate

@Component
class DailyTypeResolver(
    private val holidayFetcher: HolidayFetcher,
    private val holidayCache: HolidayCache
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
        return holidayCache.getHolidays(date.year)?.contains(date) ?: run {
            val holidays = holidayFetcher.fetch(date.year)
            holidayCache.cacheHolidays(holidays)
            holidays.contains(date)
        }
    }
}
