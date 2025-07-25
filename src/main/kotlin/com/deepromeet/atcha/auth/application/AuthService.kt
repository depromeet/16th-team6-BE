package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.ProviderToken
import com.deepromeet.atcha.auth.domain.SignUpInfo
import com.deepromeet.atcha.auth.domain.UserAuthInfo
import com.deepromeet.atcha.auth.domain.UserTokenInfo
import com.deepromeet.atcha.auth.exception.AuthError
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.shared.web.token.JwtTokeParser
import com.deepromeet.atcha.shared.web.token.JwtTokenGenerator
import com.deepromeet.atcha.shared.web.token.TokenExpirationManager
import com.deepromeet.atcha.shared.web.token.TokenType
import com.deepromeet.atcha.user.application.UserAppender
import com.deepromeet.atcha.user.application.UserReader
import com.deepromeet.atcha.user.application.UserUpdater
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authProviders: AuthProviders,
    private val jwtTokenGenerator: JwtTokenGenerator,
    private val jwtTokeParser: JwtTokeParser,
    private val tokenExpirationManager: TokenExpirationManager,
    private val userReader: UserReader,
    private val userAppender: UserAppender,
    private val userUpdater: UserUpdater,
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
        val token = jwtTokenGenerator.generateTokens(savedUser.id)
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
        userUpdater.updateFcmToken(user, fcmToken)

        val userProvider = userProviderReader.read(user.id)
        userProviderAppender.updateProviderToken(userProvider, providerToken.token)
        val token = jwtTokenGenerator.generateTokens(user.id)

        return UserAuthInfo(user, token)
    }

    @Transactional
    fun logout(refreshToken: String) {
        tokenExpirationManager.validateNotExpired(refreshToken)
        jwtTokeParser.validateToken(refreshToken, TokenType.REFRESH)
        tokenExpirationManager.expireTokensWithRefreshToken(refreshToken)
    }

    @Transactional
    fun reissueToken(refreshToken: String): UserTokenInfo {
        tokenExpirationManager.validateNotExpired(refreshToken)
        jwtTokeParser.validateToken(refreshToken, TokenType.REFRESH)
        val userId = jwtTokeParser.getUserId(refreshToken, TokenType.REFRESH)

        tokenExpirationManager.expireTokensWithRefreshToken(refreshToken)

        val tokenInfo = jwtTokenGenerator.generateTokens(userId)

        return UserTokenInfo(userId, tokenInfo)
    }
}
