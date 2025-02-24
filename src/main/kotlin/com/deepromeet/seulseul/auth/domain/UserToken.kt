package com.deepromeet.seulseul.auth.domain

import com.deepromeet.seulseul.common.BaseTimeEntity
import com.deepromeet.seulseul.common.token.TokenInfo
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
    var accessToken: String,
    var refreshToken: String,
) : BaseTimeEntity() {
    constructor(userId: Long, tokenInfo: TokenInfo) : this(
        userId = userId,
        accessToken = tokenInfo.accessToken,
        refreshToken = tokenInfo.refreshToken
    )

    override fun toString(): String {
        return "UserToken(id=$id, userId=$userId, accessToken='$accessToken', refreshToken='$refreshToken')"
    }
}
