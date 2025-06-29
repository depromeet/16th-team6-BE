package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayJsonResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.SubwayStationResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.SubwayTimeResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-subway-info-client",
    url = "\${open-api.api.url.subway}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSubwayInfoFeignClient {
    @GetMapping("/getKwrdFndSubwaySttnList")
    fun getStationByName(
        @RequestParam serviceKey: String,
        @RequestParam subwayStationName: String? = null,
        @RequestParam _type: String = "json",
        @RequestParam numOfRows: Int = 2000
    ): PublicSubwayJsonResponse<List<SubwayStationResponse>>

    @GetMapping("/getSubwaySttnAcctoSchdulList")
    fun getStationSchedule(
        @RequestParam serviceKey: String,
        @RequestParam subwayStationId: String,
        @RequestParam dailyTypeCode: String,
        @RequestParam upDownTypeCode: String,
        @RequestParam _type: String = "json",
        @RequestParam numOfRows: Int = 2000
    ): PublicSubwayJsonResponse<List<SubwayTimeResponse>>
}
