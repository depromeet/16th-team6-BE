package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusPositionResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-seoul-bus-position",
    url = "\${open-api.api.url.bus-position}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSeoulBusPositionFeignClient {
    @GetMapping("/getBusPosByRtid")
    fun getBusPosByRtid(
        @RequestParam serviceKey: String,
        @RequestParam busRouteId: String
    ): ServiceResult<SeoulBusPositionResponse>
}
