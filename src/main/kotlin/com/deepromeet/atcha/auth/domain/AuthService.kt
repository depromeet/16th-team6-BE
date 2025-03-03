package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
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
    ): UserTokenInfo {
        val provider = Provider(ProviderType.findByOrdinal(signUpInfo.provider), providerToken)
        val authProvider = authProviders.getAuthProvider(provider)
        val providerUserInfo = authProvider.getUserInfo(providerToken)

        if (userReader.checkExists(providerUserInfo.providerId)) { // todo uk로 예외 처리
            throw AuthException.AlreadyExistsUser
        }

        val savedUser = userAppender.save(providerUserInfo, signUpInfo)
        val token = tokenGenerator.generateTokens(savedUser.id)
        val userToken = UserToken(savedUser.id, provider, token)

        userTokenAppender.save(userToken)

        return userToken
    }

    @Transactional
    fun login(
        providerToken: String,
        providerOrdinal: Int
    ): UserTokenInfo {
        val provider = Provider(ProviderType.findByOrdinal(providerOrdinal), providerToken)
        val authProvider = authProviders.getAuthProvider(provider.providerType)

        val userInfo = authProvider.getUserInfo(provider.providerToken)
        val user = userReader.readByProviderId(userInfo.providerId)

        val token = tokenGenerator.generateTokens(user.id)
        val userTokenInfo = UserTokenInfo(user.id, token)

        userTokenAppender.save(userTokenInfo)

        return userTokenInfo
    }

    @Transactional
    fun logout(accessToken: String) {
        tokenGenerator.validateToken(accessToken, TokenType.ACCESS)

        val userToken = userTokenReader.readByAccessToken(accessToken)
        tokenGenerator.expireToken(userToken.accessToken)
        tokenGenerator.expireToken(userToken.refreshToken)

        val authProvider = authProviders.getAuthProvider(userToken.provider)
        authProvider.logout(userToken.provider.providerToken)
    }

    @Transactional
    fun reissueToken(refreshToken: String): UserToken {
        tokenGenerator.validateToken(refreshToken, TokenType.REFRESH)

        val userToken = userTokenReader.readByRefreshToken(refreshToken)
        tokenGenerator.expireToken(userToken.accessToken)
        tokenGenerator.expireToken(userToken.refreshToken)

        val newTokenInfo = tokenGenerator.generateTokens(userToken.userId)
        userTokenAppender.update(userToken, newTokenInfo)

        return userToken
    }
}
