package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.UserId

data class UserProvider(
    val id: UserProviderId,
    val userId: UserId,
    val providerContext: ProviderContext
) {
    companion object {
        fun create(
            userId: UserId,
            providerContext: ProviderContext
        ): UserProvider {
            return UserProvider(
                id = UserProviderId(0L),
                userId = userId,
                providerContext = providerContext
            )
        }
    }

    fun updateProviderToken(newToken: String): UserProvider {
        return copy(providerContext = providerContext.updateToken(newToken))
    }
}
