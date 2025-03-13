package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.auth.infrastructure.response.KakaoUserInfoResponse
import com.deepromeet.atcha.common.feign.FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "kakaoApi",
    url = "\${kakao.api.url}",
    configuration = [KakaoFeignConfig::class, FeignConfig::class]
)
interface KakaoFeignClient {
    @GetMapping("/v2/user/me")
    fun getUserInfo(
        @RequestHeader("Authorization") providerToken: String
    ): KakaoUserInfoResponse

    @GetMapping("/v1/user/logout")
    fun logout(
        @RequestHeader("Authorization") providerToken: String
    )
}
