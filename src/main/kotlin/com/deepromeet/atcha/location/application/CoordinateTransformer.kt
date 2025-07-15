package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate

interface CoordinateTransformer {
    fun transformToWGS84(
        x: String,
        y: String
    ): Coordinate
}
