package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.common.BaseTimeEntity
import com.deepromeet.atcha.common.token.TokenInfo
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class UserToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    @Embedded
    val provider: Provider,
    var accessToken: String,
    var refreshToken: String,
) : BaseTimeEntity() {
    constructor(userId: Long, provider: Provider, tokenInfo: TokenInfo) : this(
        userId = userId,
        provider = provider,
        accessToken = tokenInfo.accessToken,
        refreshToken = tokenInfo.refreshToken
    )

    override fun toString(): String {
        return "UserToken(id=$id, userId=$userId, provider=$provider, accessToken='$accessToken', refreshToken='$refreshToken')"
    }
}
