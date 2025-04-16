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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Address

        if (address != other.address) return false
        if (lat != other.lat) return false
        if (lon != other.lon) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lon.hashCode()
        return result
    }


}
