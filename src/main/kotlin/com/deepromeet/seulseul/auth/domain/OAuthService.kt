package com.deepromeet.seulseul.auth.domain

import com.deepromeet.seulseul.auth.api.controller.log
import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.domain.response.ExistsUserResponse
import com.deepromeet.seulseul.auth.exception.AuthException
import com.deepromeet.seulseul.auth.infrastructure.client.KakaoApiClient
import com.deepromeet.seulseul.auth.infrastructure.client.Provider
import com.deepromeet.seulseul.user.domain.User
import com.deepromeet.seulseul.user.domain.UserReader
import org.springframework.stereotype.Service

@Service
class OAuthService(
    private val kakaoApiClient: KakaoApiClient,
    private val userReader: UserReader
) {
    fun checkUserExists(authorizationHeader: String, provider: Provider) : ExistsUserResponse {
        if (provider == Provider.KAKAO) {
            val kakaoUserInfo = kakaoApiClient.getUserInfo(authorizationHeader)
            return ExistsUserResponse(userReader.checkExists(kakaoUserInfo.kakaoId))
        }
        throw AuthException.NoMatchedProvider
    }

    fun signUp(authorizationHeader: String, signUpRequest: SignUpRequest) {
        val kakaoUserInfo = kakaoApiClient.getUserInfo(authorizationHeader)
        if (userReader.checkExists(kakaoUserInfo.kakaoId)) { // todo save 과정에서 uk로 예외처리
            throw AuthException.AlreadyExistsUser
        }
        val user = User(
            kakaoId = kakaoUserInfo.kakaoId,
            nickname = kakaoUserInfo.nickname,
            thumbnailImageUrl = kakaoUserInfo.thumbnailImageUrl,
            profileImageUrl = kakaoUserInfo.profileImageUrl
        )
        val savedUser = userReader.save(user)
        log.info { "User SingUp Success $savedUser" }
    }

    fun logout(authorizationHeader: String) {
        kakaoApiClient.logout(authorizationHeader)
    }
}
