package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.shared.web.token.TokenInfo
import com.deepromeet.atcha.user.domain.User

data class UserAuthInfo(
    val userTokens: UserTokens,
    val coordinate: Coordinate
) {
    constructor(
        user: User,
        token: TokenInfo
    ) : this(
        userTokens = UserTokens(user.id, token),
        coordinate = user.homeAddress.coordinate
    )
}
