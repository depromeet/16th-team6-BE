package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.api.controller.log
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.Provider
import com.deepromeet.atcha.common.token.TokenGenerator
import com.deepromeet.atcha.common.token.TokenType
import com.deepromeet.atcha.user.domain.UserAppender
import com.deepromeet.atcha.user.domain.UserReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authProviders: AuthProviders,
    private val tokenGenerator: TokenGenerator,
    private val userReader: UserReader,
    private val userAppender: UserAppender,
    private val userTokenReader: UserTokenReader,
    private val userTokenAppender: UserTokenAppender
) {
    @Transactional(readOnly = true)
    fun checkUserExists(
        providerToken: String,
        providerOrdinal: Int
    ): Boolean {
        val authProvider = authProviders.getAuthProvider(providerOrdinal)
        val userInfo = authProvider.getUserInfo(providerToken)
        return userReader.checkExists(userInfo.providerId)
    }

    @Transactional
    fun signUp(
        providerToken: String,
        signUpInfo: SignUpInfo
    ): UserToken {
        val provider = Provider.findByOrdinal(signUpInfo.provider)
        val authClient = authProviders.getAuthProvider(provider.ordinal)
        val providerUserInfo = authClient.getUserInfo(providerToken)

        if (userReader.checkExists(providerUserInfo.providerId)) { // todo uk로 예외 처리
            throw AuthException.AlreadyExistsUser
        }

        val savedUser = userAppender.save(providerUserInfo, signUpInfo)
        val token = tokenGenerator.generateTokens(savedUser.id)
        val userToken = UserToken(savedUser.id, provider, providerToken, token)

        userTokenAppender.save(userToken)

        log.info { "SingUp Success!! userToken=$userToken" }

        return userToken
    }

    @Transactional
    fun login(
        providerToken: String,
        providerOrdinal: Int
    ): UserToken {
        val provider = Provider.findByOrdinal(providerOrdinal)
        val authClient = authProviders.getAuthProvider(provider.ordinal)

        val userInfo = authClient.getUserInfo(providerToken)
        val user = userReader.readByProviderId(userInfo.providerId)

        val token = tokenGenerator.generateTokens(user.id)
        val userToken = UserToken(user.id, provider, providerToken, token)

        userTokenAppender.save(userToken)

        log.info { "Login Success!! userToken=$userToken"}

        return userToken
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
    fun reissueToken(refreshToken: String): UserToken {
        tokenGenerator.validateToken(refreshToken, TokenType.REFRESH)
        val userToken = userTokenReader.readByRefreshToken(refreshToken)
        tokenGenerator.expireToken(userToken.accessToken)
        tokenGenerator.expireToken(userToken.refreshToken)
        val newTokenInfo = tokenGenerator.generateTokens(userToken.userId)
        userToken.let {
            it.accessToken = newTokenInfo.accessToken
            it.refreshToken = newTokenInfo.refreshToken
        }
        return userToken
    }
}
