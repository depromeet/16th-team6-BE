package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiApiResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-gyeonggi-route-info",
    url = "\${open-api.api.url.gyeonggi-route}",
    configuration = [PublicFeignConfig::class]
)
interface PublicGyeonggiRouteInfoFeignClient {
    @GetMapping("/getBusRouteStationListv2")
    fun getRouteStationList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "json"
    ): PublicGyeonggiApiResponse<PublicGyeonggiResponse.BusRouteStationListResponse>

    @GetMapping("/getBusRouteInfoItemv2")
    fun getRouteInfo(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "json"
    ): PublicGyeonggiApiResponse<PublicGyeonggiResponse.BusRouteInfoResponse>
}
