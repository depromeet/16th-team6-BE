package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.repository.UserProviderRepository
import org.springframework.stereotype.Component

@Component
class UserProviderAppender(
    private val userProviderRepository: UserProviderRepository
) {
    fun save(
        userId: Long,
        provider: Provider
    ) = userProviderRepository.save(UserProvider(userId, provider))
}
