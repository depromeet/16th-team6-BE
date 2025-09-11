package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.GyeonggiBusLocationListResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.PublicGyeonggiResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicGyeonggiBusPositionHttpClient {
    @GetExchange("/getBusLocationListv2")
    suspend fun getBusLocationList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "xml"
    ): PublicGyeonggiResponse<GyeonggiBusLocationListResponse>
}
