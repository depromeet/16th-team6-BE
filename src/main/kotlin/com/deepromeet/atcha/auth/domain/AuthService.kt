package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.api.controller.log
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
    private val userProviderAppender: UserProviderAppender,
    private val userProviderReader: UserProviderReader
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
        val providerType = ProviderType.findByOrdinal(signUpInfo.provider)
        val authProvider = authProviders.getAuthProvider(providerType)
        val providerUserInfo = authProvider.getUserInfo(providerToken)
        if (userReader.checkExists(providerUserInfo.providerId)) { // todo uk로 예외 처리
            throw AuthException.AlreadyExistsUser
        }

        val savedUser = userAppender.save(providerUserInfo, signUpInfo)
        val token = tokenGenerator.generateTokens(savedUser.id)

        userProviderAppender.save(savedUser.id, Provider(providerType, providerToken))

        log.info { "SingUp Success!! user=$savedUser" }

        return UserTokenInfo(savedUser.id, token)
    }

    @Transactional
    fun login(
        providerToken: String,
        providerOrdinal: Int
    ): UserTokenInfo {
        val providerType = ProviderType.findByOrdinal(providerOrdinal)
        val authProvider = authProviders.getAuthProvider(providerType)

        val userInfo = authProvider.getUserInfo(providerToken)
        val user = userReader.readByProviderId(userInfo.providerId)

        val token = tokenGenerator.generateTokens(user.id)

        log.info { "Login Success!! user=$user" }

        return UserTokenInfo(user.id, token)
    }

    @Transactional
    fun logout(refreshToken: String) {
        tokenGenerator.validateToken(refreshToken, TokenType.REFRESH)
        val userId = tokenGenerator.getUserIdByToken(refreshToken, TokenType.REFRESH)

        val userProvider = userProviderReader.read(userId)
        val authProvider = authProviders.getAuthProvider(userProvider.provider)
        authProvider.logout(userProvider.provider)

        tokenGenerator.expireTokensWithRefreshToken(refreshToken)

        log.info { "Logout Success!! userId = $userId" }
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
