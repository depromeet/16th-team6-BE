package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.repository.UserTokenRepository
import org.springframework.stereotype.Component

@Component
class UserTokenReader(
    private val userTokenRepository: UserTokenRepository
) {
    fun save(userToken: UserToken): UserToken = userTokenRepository.save(userToken)

    fun findByUserId(userId: Long): UserToken =
        userTokenRepository.findByUserId(userId)
            ?: throw AuthException.NoMatchedUserToken

    fun findByRefreshToken(refreshToken: String): UserToken =
        userTokenRepository.findByRefreshToken(refreshToken)
            ?: throw AuthException.NoMatchedUserToken

    fun findByAccessToken(accessToken: String): UserToken =
        userTokenRepository.findByAccessToken(accessToken)
            ?: throw AuthException.NoMatchedUserToken
}
