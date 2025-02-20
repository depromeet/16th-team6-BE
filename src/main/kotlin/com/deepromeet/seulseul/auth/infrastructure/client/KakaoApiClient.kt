package com.deepromeet.seulseul.auth.infrastructure.client

import com.deepromeet.seulseul.auth.api.controller.LoggingConfiguration
import com.deepromeet.seulseul.auth.infrastructure.response.KaKaoOAuthTokenInfoResponse
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoUserInfoResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "kakaoApi",
    url = "\${kakao.api.url}",
    configuration = [kakaoFeignConfig::class, LoggingConfiguration::class]
)
interface KakaoApiClient {
    @GetMapping("/v1/user/access_token_info")
    fun getTokenInfo(@RequestHeader("Authorization") accessToken : String) : KaKaoOAuthTokenInfoResponse

    @GetMapping("/v2/user/me")
    fun getUserInfo(@RequestHeader("Authorization") accessToken : String) : KakaoUserInfoResponse

    @GetMapping("/v1/user/logout")
    fun logout(@RequestHeader("Authorization") accessToken : String)
}
