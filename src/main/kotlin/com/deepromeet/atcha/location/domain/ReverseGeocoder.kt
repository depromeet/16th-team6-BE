package com.deepromeet.atcha.location.domain

interface ReverseGeocoder {
    fun geocode(coordinate: Coordinate): Location
}
