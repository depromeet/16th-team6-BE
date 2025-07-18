package com.deepromeet.atcha.transit.infrastructure.client.public.incheon

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response.IncheonBusPositionResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-incheon-bus-position-feign-client",
    url = "\${open-api.api.url.incheon-position}",
    configuration = [PublicFeignConfig::class]
)
interface PublicIncheonBusPositionFeignClient {
    @GetMapping("/getBusRouteLocation")
    fun getBusRouteLocation(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusPositionResponse>
}
