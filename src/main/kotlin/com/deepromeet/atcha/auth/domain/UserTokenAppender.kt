package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.repository.UserTokenRepository
import com.deepromeet.atcha.common.token.TokenInfo
import org.springframework.stereotype.Component

@Component
class UserTokenAppender(
    private val userTokenRepository: UserTokenRepository
) {
    fun save(userToken: UserToken): UserToken = userTokenRepository.save(userToken)

    fun update(
        userToken: UserToken,
        newTokenInfo: TokenInfo
    ) {
        userToken.apply {
            accessToken = newTokenInfo.accessToken
            refreshToken = newTokenInfo.refreshToken
        }
    }
}
