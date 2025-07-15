package com.deepromeet.atcha.transit.application

import java.time.LocalDate

interface HolidayCache {
    fun cacheHolidays(holidays: List<LocalDate>)

    fun getHolidays(year: Int): List<LocalDate>?
}
