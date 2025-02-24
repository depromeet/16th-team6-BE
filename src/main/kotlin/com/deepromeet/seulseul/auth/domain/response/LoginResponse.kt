package com.deepromeet.seulseul.auth.domain.response

import com.deepromeet.seulseul.common.token.TokenInfo
import com.deepromeet.seulseul.user.domain.User

data class LoginResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(user: User, tokenInfo: TokenInfo) : this(user.id, tokenInfo.accessToken, tokenInfo.refreshToken)
}
