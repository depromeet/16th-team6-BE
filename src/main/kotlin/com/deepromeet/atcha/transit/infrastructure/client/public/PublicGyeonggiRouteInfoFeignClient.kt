package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiResponse
import feign.Headers
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-gyeonggi-route-info",
    url = "\${open-api.api.url.gyeonggi-route}",
    configuration = [PublicFeignConfig::class]
)
interface PublicGyeonggiRouteInfoFeignClient {
    @GetMapping("/getBusRouteStationListv2")
    @Headers("Accept: application/xml")
    fun getRouteStationList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "xml",
        @RequestHeader("Accept") acceptHeader: String = "application/xml"
    ): PublicGyeonggiResponse<PublicGyeonggiResponse.BusRouteStationListResponse>

    @GetMapping("/getBusRouteInfoItemv2")
    @Headers("Accept: application/xml")
    fun getRouteInfo(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "xml",
        @RequestHeader("Accept") acceptHeader: String = "application/xml"
    ): PublicGyeonggiResponse<PublicGyeonggiResponse.BusRouteInfoResponse>
}
