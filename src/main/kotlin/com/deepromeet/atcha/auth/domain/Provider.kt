package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType

data class Provider(
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
        ): Provider {
            return Provider(
                providerUserId = providerUserId,
                providerType = providerToken.providerType,
                providerToken = providerToken.token
            )
        }
    }

    fun updateToken(newToken: String): Provider {
        require(newToken.isNotBlank()) { "New token cannot be blank" }
        return copy(providerToken = newToken)
    }
}
