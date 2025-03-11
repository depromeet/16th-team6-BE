package com.deepromeet.atcha.transit.infrastructure.client.kakao

import com.deepromeet.atcha.transit.infrastructure.client.kakao.response.KakaoResponse
import com.deepromeet.atcha.transit.infrastructure.client.public.config.KakaoRegionFeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "kakao-region",
    url = "\${kakao.local.url}",
    configuration = [KakaoRegionFeignConfig::class]
)
interface KakaoRegionFeignClient {
    @GetMapping("/geo/coord2regioncode.json")
    fun getRegion(
        @RequestParam x: String,
        @RequestParam y: String
    ): KakaoResponse
}
