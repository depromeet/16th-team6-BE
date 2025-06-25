package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType

data class ProviderToken(
    val token: String,
    val providerType: ProviderType
) {
    companion object {
        fun of(
            token: String,
            providerOrdinal: Int
        ): ProviderToken {
            return ProviderToken(
                token = token,
                providerType = ProviderType.findByOrdinal(providerOrdinal)
            )
        }
    }
}
