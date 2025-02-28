package com.deepromeet.atcha.auth.api.request

import com.deepromeet.atcha.auth.domain.SignUpInfo

data class SignUpRequest(
    val provider: Int,
    val address: String,
    val lat: Double,
    val log: Double,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean
) {
    fun toSignUpInfo() = SignUpInfo(
        provider = provider,
        address = address,
        lat = lat,
        log = log,
        alertAgreement = alertAgreement,
        trackingAgreement = trackingAgreement
    )
}
