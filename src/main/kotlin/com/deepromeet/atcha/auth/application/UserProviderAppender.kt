package com.deepromeet.atcha.auth.application

import com.deepromeet.atcha.auth.domain.Provider
import com.deepromeet.atcha.auth.domain.UserProvider
import com.deepromeet.atcha.auth.infrastructure.repository.UserProviderRepository
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.infrastructure.mapper.UserMapper
import org.springframework.stereotype.Component

@Component
class UserProviderAppender(
    private val userProviderRepository: UserProviderRepository,
    private val userMapper: UserMapper
) {
    fun append(
        user: User,
        provider: Provider
    ) = userProviderRepository.save(UserProvider(userMapper.toEntity(user), provider))

    fun updateProviderToken(
        userProvider: UserProvider,
        providerToken: String
    ) {
        userProvider.provider.providerToken = providerToken
    }
}
