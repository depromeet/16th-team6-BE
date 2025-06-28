package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.IncheonBusPositionResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
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
