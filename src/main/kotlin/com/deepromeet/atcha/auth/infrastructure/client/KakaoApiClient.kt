package com.deepromeet.atcha.auth.infrastructure.client

import com.deepromeet.atcha.auth.api.controller.LoggingConfiguration
import com.deepromeet.atcha.auth.infrastructure.response.KakaoUserInfoResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "kakaoApi",
    url = "\${kakao.api.url}",
    configuration = [KakaoFeignConfig::class, LoggingConfiguration::class]
)
interface KakaoApiClient {
    @GetMapping("/v2/user/me")
    fun getUserInfo(@RequestHeader("Authorization") providerToken : String) : KakaoUserInfoResponse

    @GetMapping("/v1/user/logout")
    fun logout(@RequestHeader("Authorization") providerToken : String)
}
