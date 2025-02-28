package com.deepromeet.atcha.location.domain

interface ReverseLabeler {
    fun label(coordinate: Coordinate): Location
}
