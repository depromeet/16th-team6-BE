package com.deepromeet.atcha.transit.infrastructure.client.kakao

import com.deepromeet.atcha.transit.infrastructure.client.kakao.response.KakaoRouteResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface KakaoRouteHttpClient {
    @GetExchange("/route/carset/mobility.json")
    fun getRoute(
        @RequestParam origin: String,
        @RequestParam destination: String
    ): KakaoRouteResponse
}
