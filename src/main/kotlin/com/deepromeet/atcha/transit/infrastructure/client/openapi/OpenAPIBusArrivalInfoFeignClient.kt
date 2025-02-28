package com.deepromeet.atcha.transit.infrastructure.client.openapi

import com.deepromeet.atcha.transit.infrastructure.client.openapi.config.OpenAPIFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.openapi.response.BusArrivalResponse
import com.deepromeet.atcha.transit.infrastructure.client.openapi.response.ServiceResult
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "open-api-bus-arrival-info",
    url = "\${open-api.api.url.bus}",
    configuration = [OpenAPIFeignConfig::class]
)
interface OpenAPIBusArrivalInfoFeignClient {
    @GetMapping("/api/rest/arrive/getArrInfoByRoute")
    fun getArrivalInfoByRoute(
        @RequestParam busRouteId: String,
        @RequestParam stId: String,
        @RequestParam ord: Int
    ): ServiceResult<BusArrivalResponse>
}
