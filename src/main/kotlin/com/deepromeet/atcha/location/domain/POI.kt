package com.deepromeet.atcha.location.domain

import com.deepromeet.atcha.common.annotation.NoArg

@NoArg
data class POI(
    val location: Location,
    val businessCategory: String,
    val address: String,
    val radius: Double? = null
) {
    fun distanceTo(other: Coordinate): POI {
        return copy(radius = location.coordinate.distanceTo(other))
    }
}
