package com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi

import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.GyeonggiBusRouteInfoResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.GyeonggiBusRouteListResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.GyeonggiBusRouteStationListResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.gyeonggi.response.PublicGyeonggiResponse
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicGyeonggiRouteInfoHttpClient {
    @GetExchange("/getBusRouteStationListv2")
    suspend fun getRouteStationList(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "xml",
        @RequestHeader("Accept") acceptHeader: String = "application/xml"
    ): PublicGyeonggiResponse<GyeonggiBusRouteStationListResponse>

    @GetExchange("/getBusRouteInfoItemv2")
    suspend fun getRouteInfo(
        @RequestParam serviceKey: String,
        @RequestParam routeId: String,
        @RequestParam format: String = "xml",
        @RequestHeader("Accept") acceptHeader: String = "application/xml"
    ): PublicGyeonggiResponse<GyeonggiBusRouteInfoResponse>

    @GetExchange("/getBusRouteListv2")
    suspend fun getRouteList(
        @RequestParam serviceKey: String,
        @RequestParam keyword: String,
        @RequestParam format: String = "xml",
        @RequestHeader("Accept") acceptHeader: String = "application/xml"
    ): PublicGyeonggiResponse<GyeonggiBusRouteListResponse>
}
