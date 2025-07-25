package com.deepromeet.atcha.user.api.request

import com.deepromeet.atcha.location.domain.Coordinate
import com.deepromeet.atcha.user.domain.HomeAddress

data class HomeAddressUpdateRequest(
    val address: String,
    val lat: Double,
    val lon: Double
) {
    fun toHomeAddress(): HomeAddress {
        return HomeAddress(
            address = address,
            coordinate = Coordinate(lat, lon)
        )
    }
}
