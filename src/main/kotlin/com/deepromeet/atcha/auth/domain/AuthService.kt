package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.api.controller.log
import com.deepromeet.atcha.auth.api.request.SignUpRequest
import com.deepromeet.atcha.auth.domain.response.ExistsUserResponse
import com.deepromeet.atcha.auth.domain.response.LoginResponse
import com.deepromeet.atcha.auth.domain.response.ReissueTokenResponse
import com.deepromeet.atcha.auth.domain.response.SignUpResponse
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.client.kakao.KakaoFeignClient
import com.deepromeet.atcha.auth.infrastructure.client.Provider
import com.deepromeet.atcha.common.token.TokenGenerator
import com.deepromeet.atcha.common.token.TokenType
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authClients: Map<String, AuthClient>,
    private val tokenGenerator: TokenGenerator,
    private val userReader: UserReader,
    private val userTokenReader: UserTokenReader
) {
    @Transactional(readOnly = true)
    fun checkUserExists(
        authorizationHeader: String,
        providerOrdinal: Int
    ): ExistsUserResponse {
        val provider = Provider.findByOrdinal(providerOrdinal)
        val authClient = authClients[provider.clientBeanName]
            ?: throw AuthException.NoMatchedProvider
        val userInfo = authClient.getUserInfo(authorizationHeader)
        return ExistsUserResponse(userReader.checkExists(userInfo.clientId))
    }

    @Transactional
    fun signUp(
        authorizationHeader: String,
        signUpRequest: SignUpRequest
    ): SignUpResponse {
        val provider = Provider.findByOrdinal(signUpRequest.provider)
        val authClient = authClients[provider.clientBeanName]
            ?: throw AuthException.NoMatchedProvider
        val userInfo = authClient.getUserInfo(authorizationHeader)

        if (userReader.checkExists(userInfo.clientId)) { // todo uk로 예외 처리
            throw AuthException.AlreadyExistsUser
        }
        val user = userInfo.toDomain().apply {
            address = signUpRequest.address
            addressLat = signUpRequest.lat
            addressLog = signUpRequest.log
        }

        val savedUser = userReader.save(user)
        val token = tokenGenerator.generateTokens(savedUser.id)
        val userToken = UserToken(savedUser.id, authorizationHeader.substring("Bearer ".length), token)

        userTokenReader.save(userToken)

        log.info { "SingUp Success!! user=$savedUser" }

        return SignUpResponse(savedUser, token)
    }

//    @Transactional
//    fun login(
//        authorizationHeader: String,
//        provider: Int
//    ): LoginResponse {
//        val kakaoUserInfo = kakaoFeignClient.getUserInfo(authorizationHeader) // todo 예외 처리
//        val user = userReader.findByKakaoId(kakaoUserInfo.kakaoId)
//        val token = tokenGenerator.generateTokens(user.id)
//        val userToken = UserToken(user.id, authorizationHeader.substring("Bearer ".length), token)
//        userTokenReader.save(userToken)
//
//        log.info { "Login Success!! userId = ${user.id} token = $userToken" }
//
//        return LoginResponse(user, token)
//    }
//
//    @Transactional
//    fun logout(accessToken: String) {
//        tokenGenerator.validateToken(accessToken, TokenType.ACCESS)
//        val userToken = userTokenReader.findByAccessToken(accessToken)
//        tokenGenerator.expireToken(userToken.accessToken)
//        tokenGenerator.expireToken(userToken.refreshToken)
//        kakaoFeignClient.logout("Bearer ${userToken.providerToken}")
//        log.info { "Logout Success!! userId = ${userToken.id}" }
//    }
//
//    @Transactional
//    fun reissueToken(refreshToken: String): ReissueTokenResponse {
//        tokenGenerator.validateToken(refreshToken, TokenType.REFRESH)
//        val userToken = userTokenReader.findByRefreshToken(refreshToken)
//        tokenGenerator.expireToken(userToken.accessToken)
//        tokenGenerator.expireToken(userToken.refreshToken)
//        val newTokenInfo = tokenGenerator.generateTokens(userToken.userId)
//        userToken.let {
//            it.accessToken = newTokenInfo.accessToken
//            it.refreshToken = newTokenInfo.refreshToken
//        }
//        return ReissueTokenResponse(newTokenInfo)
//    }
}
