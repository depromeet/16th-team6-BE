package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.Address

data class SignUpInfo(
    val provider: Int,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertFrequencies: Set<Int>
) {
    fun getAddress(): Address = Address(address, lat, lon)
}
