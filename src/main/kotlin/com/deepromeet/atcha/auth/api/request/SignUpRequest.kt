package com.deepromeet.atcha.auth.api.request

data class SignUpRequest(
    val provider: Int,
    val address: String,
    val lat: Double,
    val log: Double,
    val agreement: AgreementRequest
)

data class AgreementRequest(
    val alert: Boolean = true,
    val tracking: Boolean = true
)
