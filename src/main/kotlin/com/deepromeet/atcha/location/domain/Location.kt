package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.annotation.NoArg

@NoArg
data class Location(
    val name: String,
    val coordinate: Coordinate
)
