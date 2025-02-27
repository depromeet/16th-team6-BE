package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.api.controller.log
import com.deepromeet.atcha.auth.api.request.SignUpRequest
import com.deepromeet.atcha.auth.domain.response.LoginResponse
import com.deepromeet.atcha.auth.domain.response.ReissueTokenResponse
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.Provider
import com.deepromeet.atcha.common.token.TokenGenerator
import com.deepromeet.atcha.common.token.TokenType
import com.deepromeet.atcha.user.domain.Address
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authProviders: AuthProviders,
    private val tokenGenerator: TokenGenerator,
    private val userReader: UserReader,
    private val userTokenReader: UserTokenReader
) {
    @Transactional(readOnly = true)
    fun checkUserExists(
        providerToken: String,
        providerOrdinal: Int
    ): Boolean {
        val authProvider = authProviders.getAuthProvider(providerOrdinal)
        val userInfo = authProvider.getUserInfo(providerToken)
        return userReader.checkExists(userInfo.clientId)
    }

    @Transactional
    fun signUp(
        providerToken: String,
        signUpRequest: SignUpRequest
    ): UserToken {
        val provider = Provider.findByOrdinal(signUpRequest.provider)
        val authClient = authProviders.getAuthProvider(provider.ordinal)
        val userInfo = authClient.getUserInfo(providerToken)

        if (userReader.checkExists(userInfo.clientId)) { // todo uk로 예외 처리
            throw AuthException.AlreadyExistsUser
        }
        val user = userInfo.toDomain().apply {
            address = Address( signUpRequest.address, signUpRequest.lat, signUpRequest.log)
        }

        val savedUser = userReader.save(user)
        val token = tokenGenerator.generateTokens(savedUser.id)
        val userToken = UserToken(savedUser.id, provider, providerToken, token)

        userTokenReader.save(userToken)

        log.info { "SingUp Success!! user=$savedUser" }

        return userToken
    }

    @Transactional
    fun login(
        providerToken: String,
        providerOrdinal: Int
    ): LoginResponse {
        val provider = Provider.findByOrdinal(providerOrdinal)
        val authClient = authProviders.getAuthProvider(provider.ordinal)

        val userInfo = authClient.getUserInfo(providerToken)
        val user = userReader.findByProviderId(userInfo.clientId)

        val token = tokenGenerator.generateTokens(user.id)
        val userToken = UserToken(user.id, provider, providerToken, token)

        userTokenReader.save(userToken)

        log.info { "Login Success!! userId = ${user.id} token = $userToken" }

        return LoginResponse(user, token)
    }

    @Transactional
    fun logout(accessToken: String) {
        tokenGenerator.validateToken(accessToken, TokenType.ACCESS)
        val userToken = userTokenReader.readByAccessToken(accessToken)
        tokenGenerator.expireToken(userToken.accessToken)
        tokenGenerator.expireToken(userToken.refreshToken)
        val provider = userToken.provider
        val authClient = authProviders.getAuthProvider(provider)
        authClient.logout("Bearer ${userToken.providerToken}")
        log.info { "Logout Success!! userId = ${userToken.id}" }
    }

    @Transactional
    fun reissueToken(refreshToken: String): ReissueTokenResponse {
        tokenGenerator.validateToken(refreshToken, TokenType.REFRESH)
        val userToken = userTokenReader.readByRefreshToken(refreshToken)
        tokenGenerator.expireToken(userToken.accessToken)
        tokenGenerator.expireToken(userToken.refreshToken)
        val newTokenInfo = tokenGenerator.generateTokens(userToken.userId)
        userToken.let {
            it.accessToken = newTokenInfo.accessToken
            it.refreshToken = newTokenInfo.refreshToken
        }
        return ReissueTokenResponse(newTokenInfo)
    }
}
