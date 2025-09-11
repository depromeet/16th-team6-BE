package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusPositionResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicSeoulBusPositionHttpClient {
    @GetExchange("/getBusPosByRtid")
    suspend fun getBusPosByRtid(
        @RequestParam serviceKey: String,
        @RequestParam busRouteId: String
    ): ServiceResult<SeoulBusPositionResponse>
}
