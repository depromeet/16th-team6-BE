package com.deepromeet.atcha.auth.api.response

import com.deepromeet.atcha.auth.domain.UserToken

data class ReissueTokenResponse(
    val accessToken: String,
    val refreshToken: String
) {
    constructor(userToken: UserToken) : this(accessToken = userToken.accessToken, refreshToken = userToken.refreshToken)
}
