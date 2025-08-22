package com.deepromeet.atcha.transit.application

import com.deepromeet.atcha.transit.domain.DailyType
import com.deepromeet.atcha.transit.domain.TransitType
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate

@Component
class DailyTypeResolver(
    private val holidayFetcher: HolidayFetcher,
    private val holidayCache: HolidayCache
) {
    suspend fun resolve(
        transitType: TransitType,
        date: LocalDate = LocalDate.now()
    ): DailyType {
        return when (transitType) {
            TransitType.SUBWAY, TransitType.BUS -> resolveDailyType(date)
        }
    }

    private suspend fun resolveDailyType(date: LocalDate): DailyType {
        val isHoliday = isHoliday(date)
        return when {
            isHoliday -> DailyType.HOLIDAY
            date.dayOfWeek == DayOfWeek.SATURDAY -> DailyType.SATURDAY
            date.dayOfWeek == DayOfWeek.SUNDAY -> DailyType.SUNDAY
            else -> DailyType.WEEKDAY
        }
    }

    private suspend fun isHoliday(date: LocalDate): Boolean {
        return holidayCache.getHolidays(date.year)?.contains(date) ?: run {
            val holidays = holidayFetcher.fetch(date.year)
            holidayCache.cacheHolidays(holidays)
            holidays.contains(date)
        }
    }
}
