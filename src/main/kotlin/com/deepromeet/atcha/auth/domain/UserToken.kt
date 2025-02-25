package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.common.BaseTimeEntity
import com.deepromeet.atcha.common.token.TokenInfo
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
    var providerToken: String,
    var accessToken: String,
    var refreshToken: String,
) : BaseTimeEntity() {
    constructor(userId: Long, providerToken: String, tokenInfo: TokenInfo) : this(
        userId = userId,
        providerToken = providerToken,
        accessToken = tokenInfo.accessToken,
        refreshToken = tokenInfo.refreshToken
    )

    override fun toString(): String {
        return "UserToken(id=$id, userId=$userId, providerToken='$providerToken', accessToken='$accessToken', refreshToken='$refreshToken')"
    }
}
