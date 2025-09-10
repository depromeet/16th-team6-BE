package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.auth.application.AuthProvider
import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.ProviderToken
import org.springframework.stereotype.Component

@Component
class KakaoProvider(
    private val kakaoFeignClient: KakaoFeignClient
) : AuthProvider {
    companion object {
        private const val TOKEN_TYPE = "Bearer "
    }

    override fun getProviderContext(providerToken: ProviderToken): ProviderContext {
        val kakaoUserInfoResponse = kakaoFeignClient.getUserInfo(TOKEN_TYPE + providerToken.token)
        return ProviderContext(
            providerUserId = kakaoUserInfoResponse.kakaoId.toString(),
            providerToken = providerToken.token,
            providerType = providerToken.providerType
        )
    }

    override fun logout(providerToken: String) {
        kakaoFeignClient.logout(TOKEN_TYPE + providerToken)
    }

    override fun logout(providerContext: ProviderContext) {
        kakaoFeignClient.logout(TOKEN_TYPE + providerContext.providerToken)
    }
}
