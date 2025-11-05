package com.deepromeet.atcha.transit.infrastructure.client.public.seoul

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusArrivalResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.seoul.response.SeoulBusRouteInfoResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicSeoulBusArrivalInfoHttpClient {
    @GetExchange("/api/rest/arrive/getArrInfoByRoute")
    suspend fun getArrivalInfoByRoute(
        @RequestParam serviceKey: String,
        @RequestParam busRouteId: String,
        @RequestParam stId: String,
        @RequestParam ord: Int
    ): ServiceResult<SeoulBusArrivalResponse>

    @GetExchange("/api/rest/busRouteInfo/getBusRouteList")
    suspend fun getBusRouteList(
        @RequestParam serviceKey: String,
        @RequestParam strSrch: String
    ): ServiceResult<SeoulBusRouteInfoResponse>
}
