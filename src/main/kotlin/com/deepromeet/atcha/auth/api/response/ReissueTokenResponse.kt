package com.deepromeet.atcha.auth.api.response

import com.deepromeet.atcha.auth.domain.UserTokens

data class ReissueTokenResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(
        userTokens: UserTokens
    ) : this(userTokens.id.value, userTokens.accessToken, userTokens.refreshToken)
}
