package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType

data class ProviderContext(
    val providerUserId: String,
    val providerType: ProviderType,
    val providerToken: String
) {
    init {
        require(providerUserId.isNotBlank()) { "Provider user ID cannot be blank" }
        require(providerToken.isNotBlank()) { "Provider token cannot be blank" }
    }

    companion object {
        fun of(
            providerUserId: String,
            providerToken: ProviderToken
        ): ProviderContext {
            return ProviderContext(
                providerUserId = providerUserId,
                providerType = providerToken.providerType,
                providerToken = providerToken.token
            )
        }
    }

    fun updateToken(newToken: String): ProviderContext {
        require(newToken.isNotBlank()) { "New token cannot be blank" }
        return copy(providerToken = newToken)
    }
}
