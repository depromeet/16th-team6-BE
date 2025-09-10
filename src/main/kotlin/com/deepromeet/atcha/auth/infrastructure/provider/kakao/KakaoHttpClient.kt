package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.auth.infrastructure.response.KakaoUserInfoResponse
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("https://kapi.kakao.com")
interface KakaoHttpClient {
    @GetExchange("/v2/user/me")
    suspend fun getUserInfo(
        @RequestHeader("Authorization") providerToken: String
    ): KakaoUserInfoResponse

    @GetExchange("/v1/user/logout")
    suspend fun logout(
        @RequestHeader("Authorization") providerToken: String
    )
}
