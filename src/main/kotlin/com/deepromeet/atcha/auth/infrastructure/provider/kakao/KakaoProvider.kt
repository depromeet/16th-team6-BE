package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.auth.domain.AuthProvider
import com.deepromeet.atcha.auth.infrastructure.response.ClientUserInfoResponse
import org.springframework.stereotype.Component

@Component
class KakaoProvider(
    private val kakaoFeignClient: KakaoFeignClient
) : AuthProvider {

    override fun getUserInfo(providerToken: String): ClientUserInfoResponse {
        val kakaoUserInfoResponse = kakaoFeignClient.getUserInfo(providerToken)
        return kakaoUserInfoResponse.toUserInfoResponse()
    }

    override fun logout(providerToken: String) {
        kakaoFeignClient.logout(providerToken)
    }
}
