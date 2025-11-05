package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.ProviderContext
import com.deepromeet.atcha.auth.domain.UserProvider
import com.deepromeet.atcha.auth.domain.UserProviderRepository
import com.deepromeet.atcha.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserProviderAppender(
    private val userProviderRepository: UserProviderRepository
) {
    fun append(
        user: User,
        providerContext: ProviderContext
    ): UserProvider = userProviderRepository.save(UserProvider.create(user.id, providerContext))

    fun updateProviderToken(
        userProvider: UserProvider,
        providerToken: String
    ): UserProvider {
        val updatedUserProvider = userProvider.updateProviderToken(providerToken)
        return userProviderRepository.save(updatedUserProvider)
    }
}
