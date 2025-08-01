package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.UserId

data class UserProvider(
    val id: UserProviderId,
    val userId: UserId,
    val provider: Provider
) {
    companion object {
        fun create(
            userId: UserId,
            provider: Provider
        ): UserProvider {
            return UserProvider(
                id = UserProviderId(0L),
                userId = userId,
                provider = provider
            )
        }
    }

    fun updateProviderToken(newToken: String): UserProvider {
        return copy(provider = provider.updateToken(newToken))
    }
}
