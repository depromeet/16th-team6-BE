package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.location.domain.Coordinate
import jakarta.persistence.Embeddable

@Embeddable
class Address(
    var address: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0
) {
    override fun toString(): String {
        return "Address(address='$address', lat=$lat, lon=$lon)"
    }

    fun resolveCoordinate(): Coordinate {
        return Coordinate(lat, lon)
    }
}
