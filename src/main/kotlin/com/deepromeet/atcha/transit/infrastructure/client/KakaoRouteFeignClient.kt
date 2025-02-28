package com.deepromeet.atcha.transit.infrastructure.client

import com.deepromeet.atcha.transit.infrastructure.client.response.KakaoRouteResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "kakao-taxi-fare", url = "https://app.map.kakao.com")
interface KakaoRouteFeignClient {
    @GetMapping("/route/carset/mobility.json")
    fun getRoute(
        @RequestParam origin: String,
        @RequestParam destination: String
    ): KakaoRouteResponse
}
