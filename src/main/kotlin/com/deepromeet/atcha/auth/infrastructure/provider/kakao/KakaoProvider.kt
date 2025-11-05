package com.deepromeet.atcha.auth.infrastructure.provider.kakao

import com.deepromeet.atcha.auth.application.AuthProvider
import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.ProviderToken
import org.springframework.stereotype.Component

@Component
class KakaoProvider(
    private val kakaoHttpClient: KakaoHttpClient
) : AuthProvider {
    companion object {
        private const val TOKEN_TYPE = "Bearer "
    }

    override suspend fun getProviderContext(providerToken: ProviderToken): ProviderContext {
        val kakaoUserInfoResponse =
            kakaoHttpClient.getUserInfo(TOKEN_TYPE + providerToken.token)
        return ProviderContext(
            providerUserId = kakaoUserInfoResponse.kakaoId.toString(),
            providerToken = providerToken.token,
            providerType = providerToken.providerType
        )
    }

    override suspend fun logout(providerToken: String) {
        kakaoHttpClient.logout(TOKEN_TYPE + providerToken)
    }

    override suspend fun logout(providerContext: ProviderContext) {
        kakaoHttpClient.logout(TOKEN_TYPE + providerContext.providerToken)
    }
}
