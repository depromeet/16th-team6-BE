package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusRouteStationResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicSeoulBusRouteInfoHttpClient {
    @GetExchange("/getStaionByRoute")
    suspend fun getStationsByRoute(
        @RequestParam busRouteId: String,
        @RequestParam serviceKey: String
    ): ServiceResult<SeoulBusRouteStationResponse>
}
