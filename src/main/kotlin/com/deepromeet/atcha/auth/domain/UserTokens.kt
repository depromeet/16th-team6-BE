package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.shared.web.token.TokenInfo
import com.deepromeet.atcha.user.domain.UserId

data class UserTokens(
    val id: UserId,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(id: UserId, tokenInfo: TokenInfo) : this(id, tokenInfo.accessToken, tokenInfo.refreshToken)
}
