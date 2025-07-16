package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.domain.HolidayFetcher
import com.deepromeet.atcha.transit.infrastructure.client.public.response.HolidayResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Component
class PublicHolidayClient(
    private val publicHolidayFeignClient: PublicHolidayFeignClient,
    @Value("\${open-api.api.service-key}")
    private val serviceKey: String,
    @Value("\${open-api.api.spare-key}")
    private val spareKey: String,
    @Value("\${open-api.api.real-last-key}")
    private val realLastKey: String
) : HolidayFetcher {
    override fun fetch(year: Int): List<LocalDate> {
        return ApiClientUtils.callApiWithRetry(
            primaryKey = serviceKey,
            spareKey = spareKey,
            realLastKey = realLastKey,
            apiCall = { key -> publicHolidayFeignClient.getPublicHoliday(key, year) },
            isLimitExceeded = { response -> isHolidayApiLimitExceeded(response) },
            processResult = { response ->
                response.body.items.item.map { it.toLocalDate() }
            },
            errorMessage = "공휴일 정보를 가져오는데 실패했습니다 - $year"
        ) ?: emptyList() // API 호출 실패 시 빈 리스트 반환
    }

    private fun isHolidayApiLimitExceeded(response: HolidayResponse): Boolean {
        val limitMessages =
            listOf(
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR",
                "LIMITED_NUMBER_OF_SERVICE_REQUESTS_PER_SECOND_EXCEEDS_ERROR"
            )

        val isLimited =
            response.header.resultCode != "00" ||
                (limitMessages.any { response.header.resultMsg.contains(it) })

        if (isLimited) {
            log.warn { "공휴일 API 요청 수 초과: ${response.header.resultMsg}" }
        }

        return isLimited
    }
}
