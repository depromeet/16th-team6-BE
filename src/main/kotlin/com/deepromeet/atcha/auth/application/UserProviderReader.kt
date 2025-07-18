package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.UserProvider
import com.deepromeet.atcha.auth.exception.AuthError
import com.deepromeet.atcha.auth.exception.AuthException
import com.deepromeet.atcha.auth.infrastructure.repository.UserProviderRepository
import org.springframework.stereotype.Component

@Component
class UserProviderReader(
    private val userProviderRepository: UserProviderRepository
) {
    fun read(userid: Long): UserProvider =
        userProviderRepository.findByUserId(userid)
            ?: throw AuthException.of(AuthError.NO_MATCHED_USER_TOKEN, userid.toString())
}
