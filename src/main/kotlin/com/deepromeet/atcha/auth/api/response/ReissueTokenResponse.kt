package com.deepromeet.atcha.auth.api.response

import com.deepromeet.atcha.auth.domain.UserTokenInfo

data class ReissueTokenResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(
        userTokenInfo: UserTokenInfo
    ) : this(userTokenInfo.id.value, userTokenInfo.accessToken, userTokenInfo.refreshToken)
}
