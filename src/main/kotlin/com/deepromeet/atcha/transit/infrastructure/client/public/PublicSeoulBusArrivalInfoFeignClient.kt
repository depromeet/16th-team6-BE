package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.BusArrivalResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "open-api-bus-arrival-info",
    url = "\${open-api.api.url.bus}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSeoulBusArrivalInfoFeignClient {
    @GetMapping("/api/rest/arrive/getArrInfoByRouteAll")
    fun getArrivalInfoByRoute(
        @RequestParam busRouteId: String,
        @RequestParam serviceKey: String
    ): ServiceResult<BusArrivalResponse>
}
