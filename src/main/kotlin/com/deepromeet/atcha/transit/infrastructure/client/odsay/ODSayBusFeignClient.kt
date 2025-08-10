package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.infrastructure.client.odsay.response.ODSayBusArrivalResponse
import com.deepromeet.atcha.transit.infrastructure.client.odsay.response.ODSayBusStationInfoResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.config.PublicFeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "odsay-bus-arrival-info",
    url = "\${odsay.api.url}",
    configuration = [PublicFeignConfig::class]
)
interface ODSayBusFeignClient {
    @GetMapping("/v1/api/searchStation")
    fun getStationByStationName(
        @RequestParam apiKey: String,
        @RequestParam stationName: String,
        @RequestParam lang: String = "0"
    ): ODSayBusArrivalResponse

    @GetMapping("/v1/api/busStationInfo")
    fun getStationInfoByStationID(
        @RequestParam apiKey: String,
        @RequestParam stationID: String,
        @RequestParam lang: String = "0"
    ): ODSayBusStationInfoResponse
}
