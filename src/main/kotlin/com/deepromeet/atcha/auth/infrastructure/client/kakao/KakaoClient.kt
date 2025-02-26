package com.deepromeet.atcha.auth.infrastructure.client.kakao

import com.deepromeet.atcha.auth.domain.AuthClient
import com.deepromeet.atcha.auth.infrastructure.response.ClientUserInfoResponse
import org.springframework.stereotype.Component

@Component
class KakaoClient(
    private val kakaoFeignClient: KakaoFeignClient
) : AuthClient {

    override fun getUserInfo(providerToken: String): ClientUserInfoResponse {
        val kakaoUserInfoResponse = kakaoFeignClient.getUserInfo(providerToken)
        return kakaoUserInfoResponse.toUserInfoResponse()
    }
}
