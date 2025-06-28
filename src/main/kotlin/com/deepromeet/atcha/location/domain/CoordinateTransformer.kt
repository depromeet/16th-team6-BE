package com.deepromeet.atcha.location.domain

interface CoordinateTransformer {
    fun transformToWGS84(
        x: String,
        y: String
    ): Coordinate
}
