package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class Provider(
    val providerUserId: String,
    @Enumerated(EnumType.STRING)
    var providerType: ProviderType,
    @Column(length = 1024)
    var providerToken: String
) {
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
}
