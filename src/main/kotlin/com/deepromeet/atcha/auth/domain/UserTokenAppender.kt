package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.repository.UserTokenRepository
import org.springframework.stereotype.Component

@Component
class UserTokenAppender(
    private val userTokenRepository: UserTokenRepository
) {
    fun save(userToken: UserToken): UserToken = userTokenRepository.save(userToken)
}
