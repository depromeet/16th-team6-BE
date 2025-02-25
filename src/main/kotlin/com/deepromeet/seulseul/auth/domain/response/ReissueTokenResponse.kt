package com.deepromeet.seulseul.auth.domain.response

import com.deepromeet.seulseul.common.token.TokenInfo

data class ReissueTokenResponse (
    val accessToken: String,
    val refreshToken: String
) {
    constructor(tokenInfo: TokenInfo) : this(accessToken = tokenInfo.accessToken, refreshToken = tokenInfo.refreshToken)
}
