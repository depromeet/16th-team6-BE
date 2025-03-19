package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.common.feign.FeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.BusPositionResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-seoul-bus-position",
    url = "\${open-api.api.url.bus-postion}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSeoulBusPositionFeignClient {
    @GetMapping("/getBusPosByRtid")
    fun getBusPosByRtid(
        @RequestParam serviceKey: String,
        @RequestParam busRouteId: String
    ): ServiceResult<BusPositionResponse>
}
