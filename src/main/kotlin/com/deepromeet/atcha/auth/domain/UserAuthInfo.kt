package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.common.token.TokenInfo
import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.user.domain.User

data class UserAuthInfo(
    val userTokenInfo: UserTokenInfo,
    val coordinate: Coordinate
) {
    constructor(
        user: User,
        token: TokenInfo
    ) : this(
        userTokenInfo = UserTokenInfo(user.id, token),
        coordinate = Coordinate(user.address.lat, user.address.lon)
    )
}
