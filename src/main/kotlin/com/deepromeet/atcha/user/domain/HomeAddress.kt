package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.location.domain.Coordinate

data class HomeAddress(
    val address: String,
    val coordinate: Coordinate
)
