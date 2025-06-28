package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.IncheonBusRouteInfoResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.IncheonBusRouteStationListResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
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
