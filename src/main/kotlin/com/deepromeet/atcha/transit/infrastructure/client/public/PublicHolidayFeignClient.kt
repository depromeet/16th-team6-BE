package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.HolidayResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicXMLResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-holiday-client",
    url = "\${open-api.api.url.holiday}",
    configuration = [PublicFeignConfig::class]
)
interface PublicHolidayFeignClient {
    @GetMapping("/getRestDeInfo")
    fun getPublicHoliday(
        @RequestParam serviceKey: String,
        @RequestParam solYear: Int,
        @RequestParam numOfRows: Int = 100
    ): PublicXMLResponse<HolidayResponse>
}
