package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.shared.web.token.TokenInfo

data class UserTokenInfo(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(id: Long, tokenInfo: TokenInfo) : this(id, tokenInfo.accessToken, tokenInfo.refreshToken)
}
