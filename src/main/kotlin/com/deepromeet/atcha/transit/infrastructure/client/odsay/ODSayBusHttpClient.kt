package com.deepromeet.atcha.transit.infrastructure.client.odsay

import com.deepromeet.atcha.transit.infrastructure.client.odsay.response.ODSayBusArrivalResponse
import com.deepromeet.atcha.transit.infrastructure.client.odsay.response.ODSayBusStationInfoResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface ODSayBusHttpClient {
    @GetExchange("/v1/api/searchStation")
    fun getStationByStationName(
        @RequestParam apiKey: String,
        @RequestParam stationName: String,
        @RequestParam lang: String = "0"
    ): ODSayBusArrivalResponse

    @GetExchange("/v1/api/busStationInfo")
    fun getStationInfoByStationID(
        @RequestParam apiKey: String,
        @RequestParam stationID: String,
        @RequestParam lang: String = "0"
    ): ODSayBusStationInfoResponse
}
