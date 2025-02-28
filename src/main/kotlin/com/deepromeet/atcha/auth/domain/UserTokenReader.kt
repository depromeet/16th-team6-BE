package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.repository.UserTokenRepository
import org.springframework.stereotype.Component

@Component
class UserTokenReader(
    private val userTokenRepository: UserTokenRepository
) {
    fun readById(userId: Long): UserToken =
        userTokenRepository.findByUserId(userId)
            ?: throw AuthException.NoMatchedUserToken

    fun readByRefreshToken(refreshToken: String): UserToken =
        userTokenRepository.findByRefreshToken(refreshToken)
            ?: throw AuthException.NoMatchedUserToken

    fun readByAccessToken(accessToken: String): UserToken =
        userTokenRepository.findByAccessToken(accessToken)
            ?: throw AuthException.NoMatchedUserToken
}
