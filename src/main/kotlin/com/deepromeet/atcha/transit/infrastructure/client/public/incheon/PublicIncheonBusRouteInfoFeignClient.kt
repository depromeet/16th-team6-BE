package com.deepromeet.atcha.transit.infrastructure.client.public.incheon

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response.IncheonBusRouteInfoResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response.IncheonBusRouteStationListResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-incheon-bus-route-info",
    url = "\${open-api.api.url.incheon-route}",
    configuration = [PublicFeignConfig::class]
)
interface PublicIncheonBusRouteInfoFeignClient {
    @GetMapping("/getBusRouteSectionList")
    fun getBusRouteSectionList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusRouteStationListResponse>

    @GetMapping("/getBusRouteId")
    fun getBusRouteInfoById(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusRouteInfoResponse>

    @GetMapping("/getBusRouteNo")
    fun getBusRouteByName(
        @RequestParam serviceKey: String,
        @RequestParam routeNo: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusRouteInfoResponse>
}
