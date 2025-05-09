package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import com.deepromeet.atcha.common.token.TokenGenerator
import com.deepromeet.atcha.common.token.TokenType
import com.deepromeet.atcha.location.domain.Coordinate
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
    ): UserAuthInfo {
        val providerType = ProviderType.findByOrdinal(signUpInfo.provider)
        val authProvider = authProviders.getAuthProvider(providerType)
        val providerUserInfo = authProvider.getUserInfo(providerToken)
        if (userReader.checkExists(providerUserInfo.providerId)) { // todo uk로 예외 처리
            throw AuthException.AlreadyExistsUser
        }

        val savedUser = userAppender.save(providerUserInfo, signUpInfo)
        val token = tokenGenerator.generateTokens(savedUser.id)

        userProviderAppender.save(savedUser, Provider(providerUserInfo.providerId, providerType, providerToken))
        val userTokenInfo = UserTokenInfo(savedUser.id, token)
        val coordinate = Coordinate(savedUser.address.lat, savedUser.address.lon)

        return UserAuthInfo(userTokenInfo, coordinate)
    }

    @Transactional
    fun login(
        providerToken: String,
        providerOrdinal: Int,
        fcmToken: String
    ): UserAuthInfo {
        val providerType = ProviderType.findByOrdinal(providerOrdinal)
        val authProvider = authProviders.getAuthProvider(providerType)

        val userInfo = authProvider.getUserInfo(providerToken)
        val user = userReader.readByProviderId(userInfo.providerId)
        userAppender.updateFcmToken(user, fcmToken)

        val userProvider = userProviderReader.read(user.id)
        userProviderAppender.updateProviderToken(userProvider, providerToken)

        val token = tokenGenerator.generateTokens(user.id)
        val userTokenInfo = UserTokenInfo(user.id, token)
        val coordinate = Coordinate(user.address.lat, user.address.lon)

        return UserAuthInfo(userTokenInfo, coordinate)
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
