package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.location.domain.Coordinate

data class UserAuthInfo(
    val userTokenInfo: UserTokenInfo,
    val coordinate: Coordinate
)
