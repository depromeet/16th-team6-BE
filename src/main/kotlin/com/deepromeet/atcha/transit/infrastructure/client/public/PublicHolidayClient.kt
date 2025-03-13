package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.HolidayFetcher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Component
class PublicHolidayClient(
    private val publicHolidayFeignClient: PublicHolidayFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String
) : HolidayFetcher {
    override fun fetch(year: Int): List<LocalDate> {
        try {
            val response = publicHolidayFeignClient.getPublicHoliday(serviceKey, year)
            return response.body
                .items
                ?.map { it.toLocalDate() }
                .orEmpty()
        } catch (e: Exception) {
            log.warn(e) { "공휴일 정보를 가져오는데 실패했습니다." }
            return emptyList()
        }
    }
}
