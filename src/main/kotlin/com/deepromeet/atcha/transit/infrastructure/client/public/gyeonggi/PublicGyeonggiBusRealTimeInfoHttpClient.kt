package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.GyeonggiBusArrivalItemResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.PublicGyeonggiResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicGyeonggiBusRealTimeInfoHttpClient {
    @GetExchange("/getBusArrivalItemv2")
    suspend fun getRealTimeInfo(
        @RequestParam serviceKey: String,
        @RequestParam stationId: String,
        @RequestParam routeId: String,
        @RequestParam staOrder: String,
        @RequestParam format: String = "xml"
    ): PublicGyeonggiResponse<GyeonggiBusArrivalItemResponse>
}
