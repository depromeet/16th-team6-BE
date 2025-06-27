package com.deepromeet.atcha.location.domain

interface CoordinateTransformer {
    fun transformToWGS84(coordinate: Coordinate): Coordinate
}
