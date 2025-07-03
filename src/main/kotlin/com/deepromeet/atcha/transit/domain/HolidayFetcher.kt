package com.deepromeet.atcha.transit.domain

import java.time.LocalDate

interface HolidayFetcher {
    suspend fun fetch(year: Int): List<LocalDate>
}
