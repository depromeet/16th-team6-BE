package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.HolidayResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicHolidayHttpClient {
    @GetExchange("/getRestDeInfo")
    suspend fun getPublicHoliday(
        @RequestParam serviceKey: String,
        @RequestParam solYear: Int,
        @RequestParam numOfRows: Int = 100
    ): HolidayResponse
}
