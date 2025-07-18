package com.deepromeet.atcha.auth.api.request

import com.deepromeet.atcha.auth.domain.SignUpInfo

data class SignUpRequest(
    val provider: Int,
    val userName: String? = null,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertFrequencies: Set<Int>,
    val fcmToken: String
) {
    fun toSignUpInfo() =
        SignUpInfo(
            address = address,
            lat = lat,
            lon = lon,
            alertFrequencies = alertFrequencies,
            fcmToken = fcmToken
        )
}
