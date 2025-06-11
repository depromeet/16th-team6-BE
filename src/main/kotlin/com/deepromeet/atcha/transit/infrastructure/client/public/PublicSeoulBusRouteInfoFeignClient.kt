package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.BusRouteStationResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-seoul-bus-route-info",
    url = "\${open-api.api.url.bus-route}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSeoulBusRouteInfoFeignClient {
    @GetMapping("/getStaionByRoute")
    fun getStationsByRoute(
        @RequestParam busRouteId: String,
        @RequestParam serviceKey: String
    ): ServiceResult<BusRouteStationResponse>
}
