package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusArrivalResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusRouteInfoResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "open-api-bus-arrival-info",
    url = "\${open-api.api.url.bus}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSeoulBusArrivalInfoFeignClient {
    @GetMapping("/api/rest/arrive/getArrInfoByRoute")
    fun getArrivalInfoByRoute(
        @RequestParam serviceKey: String,
        @RequestParam busRouteId: String,
        @RequestParam stId: String,
        @RequestParam ord: Int
    ): ServiceResult<SeoulBusArrivalResponse>

    @GetMapping("/api/rest/busRouteInfo/getBusRouteList")
    fun getBusRouteList(
        @RequestParam serviceKey: String,
        @RequestParam strSrch: String
    ): ServiceResult<SeoulBusRouteInfoResponse>
}
