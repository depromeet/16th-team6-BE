package com.deepromeet.seulseul.auth.domain

import com.deepromeet.seulseul.auth.exception.AuthException
import com.deepromeet.seulseul.auth.infrastructure.repository.UserTokenRepository
import org.springframework.stereotype.Component

@Component
class UserTokenReader(
    private val userTokenRepository: UserTokenRepository
) {
    fun save(userToken: UserToken) : UserToken = userTokenRepository.save(userToken)

    fun findByUserId(userId: Long) : UserToken = userTokenRepository.findByUserId(userId)
        ?: throw AuthException.NoMatchedUserToken

    fun findByRefreshToken(refreshToken: String) : UserToken = userTokenRepository.findByRefreshToken(refreshToken)
        ?: throw AuthException.NoMatchedUserToken

}
