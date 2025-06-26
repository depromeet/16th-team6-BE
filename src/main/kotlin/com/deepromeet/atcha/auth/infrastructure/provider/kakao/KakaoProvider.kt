package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.auth.domain.AuthProvider
import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.ProviderToken
import org.springframework.stereotype.Component

@Component
class KakaoProvider(
    private val kakaoFeignClient: KakaoFeignClient
) : AuthProvider {
    companion object {
        private const val TOKEN_TYPE = "Bearer "
    }

    override fun getProviderUserId(providerToken: ProviderToken): Provider {
        val kakaoUserInfoResponse = kakaoFeignClient.getUserInfo(TOKEN_TYPE + providerToken.token)
        return Provider(
            providerUserId = kakaoUserInfoResponse.kakaoId.toString(),
            providerToken = providerToken.token,
            providerType = providerToken.providerType
        )
    }

    override fun logout(providerToken: String) {
        kakaoFeignClient.logout(TOKEN_TYPE + providerToken)
    }

    override fun logout(provider: Provider) {
        kakaoFeignClient.logout(TOKEN_TYPE + provider.providerToken)
    }
}
