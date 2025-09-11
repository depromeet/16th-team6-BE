package com.deepromeet.atcha.transit.infrastructure.client.public.incheon

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.ServiceResult
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response.IncheonBusRouteInfoResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.incheon.response.IncheonBusRouteStationListResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicIncheonBusRouteInfoHttpClient {
    @GetExchange("/getBusRouteSectionList")
    suspend fun getBusRouteSectionList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusRouteStationListResponse>

    @GetExchange("/getBusRouteId")
    suspend fun getBusRouteInfoById(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusRouteInfoResponse>

    @GetExchange("/getBusRouteNo")
    suspend fun getBusRouteByName(
        @RequestParam serviceKey: String,
        @RequestParam routeNo: String,
        @RequestParam pageNo: Int = 1,
        @RequestParam numOfRows: Int = 1000
    ): ServiceResult<IncheonBusRouteInfoResponse>
}
