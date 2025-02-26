package com.deepromeet.atcha.location.domain

data class POI(
    val location: Location,
    val businessCategory: String,
    val address: String,
    val radius: Int
)
