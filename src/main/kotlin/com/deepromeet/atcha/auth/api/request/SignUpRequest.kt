package com.deepromeet.atcha.auth.api.request

data class SignUpRequest(
    val address: String,
    val lat: String,
    val log: String,
    val terms: Terms
)

data class Terms(
    val alert: Boolean = true,
    val tracking: Boolean = true
)
