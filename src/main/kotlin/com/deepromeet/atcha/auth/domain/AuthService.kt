package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthError
import com.deepromeet.atcha.auth.exception.AuthException
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
    private val userProviderAppender: UserProviderAppender,
    private val userProviderReader: UserProviderReader
) {
    @Transactional(readOnly = true)
    fun checkUserExists(providerToken: ProviderToken): Boolean {
        val authProvider = authProviders.getAuthProvider(providerToken.providerType)
        val provider = authProvider.getProviderUserId(providerToken)
        return userReader.checkExists(provider.providerUserId)
    }

    @Transactional
    fun signUp(
        providerToken: ProviderToken,
        signUpInfo: SignUpInfo
    ): UserAuthInfo {
        val authProvider = authProviders.getAuthProvider(providerToken.providerType)
        val provider = authProvider.getProviderUserId(providerToken)

        if (userReader.checkExists(provider.providerUserId)) { // todo uk로 예외 처리
            throw AuthException.of(AuthError.ALREADY_EXISTS_USER)
        }

        val savedUser = userAppender.append(provider, signUpInfo)
        val token = tokenGenerator.generateTokens(savedUser.id)
        return UserAuthInfo(savedUser, token)
    }

    @Transactional
    fun login(
        providerToken: ProviderToken,
        fcmToken: String
    ): UserAuthInfo {
        val authProvider = authProviders.getAuthProvider(providerToken.providerType)

        val userInfo = authProvider.getProviderUserId(providerToken)
        val user = userReader.readByProviderId(userInfo.providerUserId)
        userAppender.updateFcmToken(user, fcmToken)

        val userProvider = userProviderReader.read(user.id)
        userProviderAppender.updateProviderToken(userProvider, providerToken.token)
        val token = tokenGenerator.generateTokens(user.id)

        return UserAuthInfo(user, token)
    }

    @Transactional
    fun logout(refreshToken: String) {
        tokenGenerator.validateToken(refreshToken, TokenType.REFRESH)
        tokenGenerator.expireTokensWithRefreshToken(refreshToken)
    }

    @Transactional
    fun reissueToken(refreshToken: String): UserTokenInfo {
        tokenGenerator.validateToken(refreshToken, TokenType.REFRESH)
        val userId = tokenGenerator.getUserIdByToken(refreshToken, TokenType.REFRESH)

        tokenGenerator.expireTokensWithRefreshToken(refreshToken)

        val tokenInfo = tokenGenerator.generateTokens(userId)

        return UserTokenInfo(userId, tokenInfo)
    }
}
