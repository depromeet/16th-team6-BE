package com.deepromeet.atcha.transit.infrastructure.client.public

import com.deepromeet.atcha.transit.infrastructure.client.public.config.PublicFeignConfig
import com.deepromeet.atcha.transit.infrastructure.client.public.response.IncheonBusStationResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.IncheonBusStationRouteResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.response.ServiceResult
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "public-incheon-bus-station-info",
    url = "\${open-api.api.url.incheon-station}",
    configuration = [PublicFeignConfig::class]
)
interface PublicIncheonBusStationInfoFeignClient {
    @GetMapping("/getBusStationNmList")
    fun getBusStationByName(
        @RequestParam serviceKey: String,
        @RequestParam bstopNm: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusStationResponse>

    @GetMapping("/getBusStationViaRouteList")
    fun getBusRoutesByStation(
        @RequestParam serviceKey: String,
        @RequestParam bstopId: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusStationRouteResponse>
}
