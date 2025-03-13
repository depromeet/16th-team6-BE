package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiApiResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-gyeonggi-bus-arrival-info",
    url = "\${open-api.api.url.gyeonggi-arrival}",
    configuration = [PublicFeignConfig::class]
)
interface PublicGyeonggiBusArrivalInfoFeignClient {
    @GetMapping("/getBusArrivalItemv2")
    fun getArrivalInfo(
        @RequestParam serviceKey: String,
        @RequestParam stationId: String,
        @RequestParam routeId: String,
        @RequestParam staOrder: String,
        @RequestParam format: String = "json"
    ): PublicGyeonggiApiResponse<PublicGyeonggiResponse.BusArrivalInfoResponse>
}
