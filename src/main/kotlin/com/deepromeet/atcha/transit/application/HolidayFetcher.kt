package com.deepromeet.atcha.transit.application

import java.time.LocalDate

interface HolidayFetcher {
    suspend fun fetch(year: Int): List<LocalDate>
}
