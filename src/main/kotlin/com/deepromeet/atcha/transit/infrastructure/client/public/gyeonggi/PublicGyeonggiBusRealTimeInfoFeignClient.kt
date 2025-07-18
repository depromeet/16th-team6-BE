package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.GyeonggiBusArrivalItemResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.PublicGyeonggiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-gyeonggi-bus-arrival-info",
    url = "\${open-api.api.url.gyeonggi-arrival}",
    configuration = [PublicFeignConfig::class]
)
interface PublicGyeonggiBusRealTimeInfoFeignClient {
    @GetMapping("/getBusArrivalItemv2")
    fun getRealTimeInfo(
        @RequestParam serviceKey: String,
        @RequestParam stationId: String,
        @RequestParam routeId: String,
        @RequestParam staOrder: String,
        @RequestParam format: String = "xml"
    ): PublicGyeonggiResponse<GyeonggiBusArrivalItemResponse>
}
