package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.BusRouteResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.response.StationResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "open-api-bus-station-info",
    url = "\${open-api.api.url.bus}",
    configuration = [PublicFeignConfig::class]
)
interface PublicSeoulBusStationInfoFeignClient {
    @GetMapping("/api/rest/stationinfo/getStationByName")
    fun getStationInfoByName(
        @RequestParam stSrch: String
    ): ServiceResult<StationResponse>

    @GetMapping("/api/rest/stationinfo/getRouteByStation")
    fun getRouteByStation(
        @RequestParam arsId: String
    ): ServiceResult<BusRouteResponse>
}
