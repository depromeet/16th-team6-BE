package com.deepromeet.seulseul.auth.domain

import com.deepromeet.seulseul.auth.api.controller.log
import com.deepromeet.seulseul.auth.api.request.SignUpRequest
import com.deepromeet.seulseul.auth.domain.response.ExistsUserResponse
import com.deepromeet.seulseul.auth.domain.response.LoginResponse
import com.deepromeet.seulseul.auth.domain.response.SignUpResponse
import com.deepromeet.seulseul.auth.exception.AuthException
import com.deepromeet.seulseul.auth.infrastructure.client.KakaoApiClient
import com.deepromeet.seulseul.auth.infrastructure.client.Provider
import com.deepromeet.seulseul.common.token.TokenGenerator
import com.deepromeet.seulseul.user.domain.UserReader
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val kakaoApiClient: KakaoApiClient,
    private val tokenGenerator: TokenGenerator,
    private val userReader: UserReader
) {
    fun checkUserExists(authorizationHeader: String, provider: Provider) : ExistsUserResponse {
        if (provider == Provider.KAKAO) {
            val kakaoUserInfo = kakaoApiClient.getUserInfo(authorizationHeader)
            return ExistsUserResponse(userReader.checkExists(kakaoUserInfo.kakaoId))
        }
        throw AuthException.NoMatchedProvider
    }

    fun signUp(authorizationHeader: String, signUpRequest: SignUpRequest) : SignUpResponse {
        val kakaoUserInfo = kakaoApiClient.getUserInfo(authorizationHeader) // todo 예외 처리
        if (userReader.checkExists(kakaoUserInfo.kakaoId)) { // todo save 과정에서 uk로 예외처리
            throw AuthException.AlreadyExistsUser
        }
        val user = kakaoUserInfo.toDomain()
        val savedUser = userReader.save(user)
        val token = tokenGenerator.generateToken(savedUser.id)

        log.info { "User SingUp Success $savedUser" }
        log.info { "generate token = $token" }

        return SignUpResponse(savedUser, token)
    }

    fun login(authorizationHeader: String, provider: Int) : LoginResponse {
        val kakaoUserInfo = kakaoApiClient.getUserInfo(authorizationHeader) // todo 예외 처리
        val user = userReader.findByKakaoId(kakaoUserInfo.kakaoId)
        val token = tokenGenerator.generateToken(user.id)
        return LoginResponse(user, token);
    }

    fun logout(authorizationHeader: String) {
        kakaoApiClient.logout(authorizationHeader)
    }
}
