package com.deepromeet.atcha.transit.infrastructure.client

import com.deepromeet.atcha.transit.infrastructure.client.request.TMapRouteRequest
import com.deepromeet.atcha.transit.infrastructure.client.response.TMapRouteResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    name = "tmap",
    url = "\${tmap.api.url}",
    configuration = [TransitFeignConfig::class],
)
interface TMapTransitClient {
    @GetMapping("/transit/routes")
    fun getRoutes(request: TMapRouteRequest): TMapRouteResponse
}
