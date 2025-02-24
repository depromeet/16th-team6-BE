package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.annotation.NoArg

@NoArg
data class Coordinate(
    val lat: Double,
    val lon: Double
)
