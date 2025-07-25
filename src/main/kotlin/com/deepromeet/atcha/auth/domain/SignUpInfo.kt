package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.HomeAddress

data class SignUpInfo(
    val userName: String? = null,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertFrequencies: Set<Int>,
    val fcmToken: String
) {
    fun getAddress(): HomeAddress = HomeAddress(address, lat, lon)
}
