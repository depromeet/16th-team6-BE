package com.deepromeet.atcha.auth.domain.response

import com.deepromeet.atcha.common.token.TokenInfo

data class ReissueTokenResponse (
    val accessToken: String,
    val refreshToken: String
) {
    constructor(tokenInfo: TokenInfo) : this(accessToken = tokenInfo.accessToken, refreshToken = tokenInfo.refreshToken)
}
