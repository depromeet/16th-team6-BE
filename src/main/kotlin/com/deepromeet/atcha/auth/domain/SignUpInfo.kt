package com.deepromeet.atcha.auth.domain

import com.deepromeet.atcha.user.domain.Address
import com.deepromeet.atcha.user.domain.Agreement

data class SignUpInfo(
    val provider: Int,
    val address: String,
    val lat: Double,
    val log: Double,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean
) {
    fun getAddress(): Address = Address(address, lat, log)

    fun getAgreement(): Agreement = Agreement(alertAgreement, trackingAgreement)
}
