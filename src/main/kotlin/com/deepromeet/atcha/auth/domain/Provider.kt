package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class Provider(
    val providerId: Long,
    @Enumerated(EnumType.STRING)
    var providerType: ProviderType,
    var providerToken: String
)
