package com.deepromeet.atcha.auth.api.request

import com.deepromeet.atcha.auth.domain.SignUpInfo

data class SignUpRequest(
    val provider: Int,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean,
    val alertFrequencies: Set<Int>
) {
    fun toSignUpInfo() =
        SignUpInfo(
            provider = provider,
            address = address,
            lat = lat,
            lon = lon,
            alertAgreement = alertAgreement,
            trackingAgreement = trackingAgreement,
            alertFrequencies = alertFrequencies
        )
}
