package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.UserProvider
import com.deepromeet.atcha.auth.domain.UserProviderRepository
import com.deepromeet.atcha.auth.exception.AuthError
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.user.domain.UserId
import org.springframework.stereotype.Component

@Component
class UserProviderReader(
    private val userProviderRepository: UserProviderRepository
) {
    fun read(userId: UserId): UserProvider =
        userProviderRepository.findByUserId(userId)
            ?: throw AuthException.of(AuthError.NO_MATCHED_USER_TOKEN, userId.value.toString())
}
