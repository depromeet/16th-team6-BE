package com.deepromeet.atcha.auth.api.response

import com.deepromeet.atcha.auth.domain.UserAuthInfo

data class LoginResponse(
    val id: Long,
    val accessToken: String,
    val refreshToken: String,
    val lat: Double,
    val lon: Double
) {
    constructor(
        userAuthInfo: UserAuthInfo
    ) : this(
        userAuthInfo.userTokenInfo.id.value,
        userAuthInfo.userTokenInfo.accessToken,
        userAuthInfo.userTokenInfo.refreshToken,
        userAuthInfo.coordinate.lat,
        userAuthInfo.coordinate.lon
    )
}
