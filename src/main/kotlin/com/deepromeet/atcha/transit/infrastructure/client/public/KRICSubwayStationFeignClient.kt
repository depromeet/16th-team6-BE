package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicSubwayStationResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "kric-subway-route-station",
    url = "\${kric.api.url.subway}"
)
interface KRICSubwayStationFeignClient {
    @GetMapping("/subwayRouteInfo")
    fun getSubwayRouteInfo(
        @RequestParam serviceKey: String,
        @RequestParam lnCd: String,
        @RequestParam format: String = "json",
        @RequestParam mreaWideCd: String = "01"
    ): PublicSubwayStationResponse
}
