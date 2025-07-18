package com.deepromeet.atcha.route.infrastructure.client.tmap

import com.deepromeet.atcha.route.infrastructure.client.tmap.request.TMapRouteRequest
import com.deepromeet.atcha.route.infrastructure.client.tmap.response.TMapRouteResponse
import com.deepromeet.atcha.transit.infrastructure.client.tmap.config.TMapFeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "tmap-transit",
    url = "\${tmap.api.url}",
    configuration = [TMapFeignConfig::class]
)
interface TMapRouteClient {
    @PostMapping("/transit/routes")
    fun getRoutes(request: TMapRouteRequest): TMapRouteResponse
}
