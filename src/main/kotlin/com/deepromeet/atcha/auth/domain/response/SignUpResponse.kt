package com.deepromeet.atcha.auth.domain.response

import com.deepromeet.atcha.common.token.TokenInfo
import com.deepromeet.atcha.user.domain.User

data class SignUpResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(user: User, tokenInfo: TokenInfo) : this(user.id, tokenInfo.accessToken, tokenInfo.refreshToken)
}
