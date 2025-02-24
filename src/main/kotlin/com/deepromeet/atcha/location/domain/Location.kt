package com.deepromeet.atcha.location.domain

data class Location(
    val name: String,
    val coordinate: Coordinate,
    val businessCategory: String,
    val address: String,
    val radius: Int
)
