package com.deepromeet.atcha.user.infrastructure.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class AddressEntity(
    @Column(name = "address")
    var address: String = "",
    @Column(name = "lat")
    var lat: Double = 0.0,
    @Column(name = "lon")
    var lon: Double = 0.0
) {
    override fun toString(): String {
        return "AddressEntity(address='$address', lat=$lat, lon=$lon)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressEntity

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
