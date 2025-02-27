package com.deepromeet.atcha.auth.api.response

import com.deepromeet.atcha.auth.domain.UserToken

data class LoginResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String
) {
    constructor(userToken: UserToken) : this(userToken.userId, userToken.accessToken, userToken.refreshToken)
}
