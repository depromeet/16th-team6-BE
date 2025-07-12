package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.PublicGyeonggiResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-gyeonggi-bus-station-info",
    url = "\${open-api.api.url.gyeonggi-bus}",
    configuration = [PublicFeignConfig::class]
)
interface PublicGyeonggiBusStationInfoFeignClient {
    @GetMapping("/getBusStationListv2")
    fun getStationList(
        @RequestParam serviceKey: String,
        @RequestParam keyword: String,
        @RequestParam format: String = "xml"
    ): PublicGyeonggiResponse<PublicGyeonggiResponse.BusStationResponse>

    @GetMapping("/getBusStationViaRouteListv2")
    fun getStationRouteList(
        @RequestParam serviceKey: String,
        @RequestParam stationId: String,
        @RequestParam format: String = "xml"
    ): PublicGyeonggiResponse<PublicGyeonggiResponse.BusRouteListResponse>
}
