package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.HolidayResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-holiday-client",
    url = "\${open-api.api.url.holiday}",
    configuration = [PublicFeignConfig::class]
)
interface PublicHolidayFeignClient {
    @GetMapping("/getRestDeInfo", produces = ["application/xml"], consumes = ["application/xml"])
    fun getPublicHoliday(
        @RequestParam serviceKey: String,
        @RequestParam solYear: Int,
        @RequestParam numOfRows: Int = 100
    ): HolidayResponse
}
