package com.deepromeet.atcha.auth.infrastructure.entity

import com.deepromeet.atcha.auth.infrastructure.provider.ProviderType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class ProviderEntity(
    val providerUserId: String,
    @Enumerated(EnumType.STRING)
    var providerType: ProviderType,
    @Column(length = 1024)
    var providerToken: String
) {
    constructor() : this("", ProviderType.KAKAO, "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProviderEntity

        if (providerUserId != other.providerUserId) return false
        if (providerType != other.providerType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = providerUserId.hashCode()
        result = 31 * result + providerType.hashCode()
        return result
    }

    override fun toString(): String {
        return "ProviderEntity(providerUserId='$providerUserId', providerType=$providerType)"
    }
}
