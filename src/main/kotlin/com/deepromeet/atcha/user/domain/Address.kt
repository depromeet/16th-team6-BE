package com.deepromeet.atcha.user.domain

import jakarta.persistence.Embeddable

@Embeddable
class Address(
    var address: String = "",
    var addressLat: Double = 0.0,
    var addressLog: Double? = 0.0,
) {
    override fun toString(): String {
        return "Address(address='$address', addressLat=$addressLat, addressLog=$addressLog)"
    }
}
