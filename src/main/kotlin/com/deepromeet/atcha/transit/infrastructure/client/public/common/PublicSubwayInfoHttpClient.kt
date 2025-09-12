package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayJsonResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.SubwayStationResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.SubwayTimeResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface PublicSubwayInfoHttpClient {
    @GetExchange("/getKwrdFndSubwaySttnList")
    suspend fun getStationByName(
        @RequestParam serviceKey: String,
        @RequestParam subwayStationName: String? = null,
        @RequestParam _type: String = "json",
        @RequestParam numOfRows: Int = 2000
    ): PublicSubwayJsonResponse<List<SubwayStationResponse>>

    @GetExchange("/getSubwaySttnAcctoSchdulList")
    suspend fun getStationSchedule(
        @RequestParam serviceKey: String,
        @RequestParam subwayStationId: String,
        @RequestParam dailyTypeCode: String,
        @RequestParam upDownTypeCode: String,
        @RequestParam _type: String = "json",
        @RequestParam numOfRows: Int = 2000
    ): PublicSubwayJsonResponse<List<SubwayTimeResponse>>
}
