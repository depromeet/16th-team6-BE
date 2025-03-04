package com.deepromeet.atcha.transit.domain

import com.deepromeet.atcha.location.domain.Coordinate

data class StationInfo(
    val name: String,
    val coordinate: Coordinate
)
