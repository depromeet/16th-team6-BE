package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.auth.infrastructure.client.Provider
import com.deepromeet.atcha.common.BaseTimeEntity
import com.deepromeet.atcha.common.token.TokenInfo
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class UserToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    @Enumerated(EnumType.STRING)
    var provider: Provider,
    var providerToken: String,
    var accessToken: String,
    var refreshToken: String,
) : BaseTimeEntity() {
    constructor(userId: Long, provider: Provider, providerToken: String, tokenInfo: TokenInfo) : this(
        userId = userId,
        provider = provider,
        providerToken = providerToken,
        accessToken = tokenInfo.accessToken,
        refreshToken = tokenInfo.refreshToken
    )

    override fun toString(): String {
        return "UserToken(id=$id, userId=$userId, providerToken='$providerToken', accessToken='$accessToken', refreshToken='$refreshToken')"
    }
}
