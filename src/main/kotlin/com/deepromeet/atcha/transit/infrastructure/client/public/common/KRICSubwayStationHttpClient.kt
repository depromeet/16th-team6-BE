package com.deepromeet.atcha.transit.infrastructure.client.public.common

import com.deepromeet.atcha.transit.infrastructure.client.public.common.response.PublicSubwayStationResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface KRICSubwayStationHttpClient {
    @GetExchange("/subwayRouteInfo")
    suspend fun getSubwayRouteInfo(
        @RequestParam serviceKey: String,
        @RequestParam lnCd: String,
        @RequestParam format: String = "json",
        @RequestParam mreaWideCd: String = "01"
    ): PublicSubwayStationResponse
}
