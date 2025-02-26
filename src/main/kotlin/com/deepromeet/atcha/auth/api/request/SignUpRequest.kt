package com.deepromeet.atcha.auth.api.request

data class SignUpRequest(
    val address: String,
    val lat: Double,
    val log: Double,
    val terms: Terms
)

data class Terms(
    val alert: Boolean = true,
    val tracking: Boolean = true
)
