package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.infrastructure.client.odsay.config.ODSayFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.odsay.request.ODSayRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.odsay.response.ODSayRouteResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "odsay-transit",
    url = "\${odsay.api.url}",
    configuration = [ODSayFeignConfig::class]
)
interface ODSayTransitClient {
    @PostMapping("/transit/routes")
    fun getRoutes(request: ODSayRouteRequest): ODSayRouteResponse
}
