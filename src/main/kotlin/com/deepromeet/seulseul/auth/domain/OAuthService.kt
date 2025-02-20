package com.deepromeet.seulseul.auth.domain

import com.deepromeet.seulseul.auth.domain.response.ExistsUserResponse
import com.deepromeet.seulseul.auth.infrastructure.response.KaKaoOAuthTokenInfoResponse
import com.deepromeet.seulseul.auth.infrastructure.client.KakaoApiClient
import com.deepromeet.seulseul.auth.infrastructure.response.KakaoUserInfoResponse
import com.deepromeet.seulseul.user.domain.UserReader
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val kakaoApiClient: KakaoApiClient,
    private val userReader: UserReader
) {
    fun getTokenInfo(authorizationHeader : String) : KaKaoOAuthTokenInfoResponse {
        return kakaoApiClient.getTokenInfo(authorizationHeader)
    }

    fun getUserInfo(authorizationHeader: String) : KakaoUserInfoResponse {
        return kakaoApiClient.getUserInfo(authorizationHeader)
    }

    fun checkUserExists(authorizationHeader: String) : ExistsUserResponse {
        val kakaoUserInfo = kakaoApiClient.getUserInfo(authorizationHeader)
        return ExistsUserResponse(userReader.checkExists(kakaoUserInfo.kakaoId))
    }

    fun logout(authorizationHeader: String) {
        kakaoApiClient.logout(authorizationHeader)
    }
}
