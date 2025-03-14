package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.repository.UserProviderRepository
import com.deepromeet.atcha.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserProviderAppender(
    private val userProviderRepository: UserProviderRepository
) {
    fun save(
        user: User,
        provider: Provider
    ) = userProviderRepository.save(UserProvider(user, provider))

    fun updateProviderToken(
        userProvider: UserProvider,
        providerToken: String
    ) {
        userProvider.provider.providerToken = providerToken
    }
}
