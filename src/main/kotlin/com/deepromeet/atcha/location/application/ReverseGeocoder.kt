package com.deepromeet.atcha.location.application

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.location.domain.Location

interface ReverseGeocoder {
    fun geocode(coordinate: Coordinate): Location
}
