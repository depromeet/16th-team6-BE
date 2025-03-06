package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.Address
import com.deepromeet.atcha.user.domain.Agreement

data class SignUpInfo(
    val provider: Int,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean,
    val alertFrequencies: Set<Int>
) {
    fun getAddress(): Address = Address(address, lat, lon)

    fun getAgreement(): Agreement = Agreement(alertAgreement, trackingAgreement)
}
