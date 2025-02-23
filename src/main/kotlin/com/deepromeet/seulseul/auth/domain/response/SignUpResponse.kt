package com.deepromeet.seulseul.auth.domain.response

import com.deepromeet.seulseul.auth.domain.TokenInfo
import com.deepromeet.seulseul.user.domain.User

data class SignUpResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(user: User, tokenInfo: TokenInfo) : this(user.id, tokenInfo.accessToken, tokenInfo.refreshToken)
}
