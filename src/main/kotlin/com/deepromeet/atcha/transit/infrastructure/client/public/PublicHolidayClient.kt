package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.HolidayFetcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class PublicHolidayClient(
    private val publicHolidayFeignClient: PublicHolidayFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : HolidayFetcher {
    override fun fetch(year: Int): List<LocalDate> {
        val response = publicHolidayFeignClient.getPublicHoliday(serviceKey, year)
        return response.body
            .items
            ?.map { it.toLocalDate() }
            .orEmpty()
    }
}
