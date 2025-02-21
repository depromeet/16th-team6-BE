package com.deepromeet.seulseul.auth.api.request

data class SignUpRequest(
    val address: String,
    val lat: String,
    val log: String,
    val terms: Terms,
)

data class Terms(
    val alert: Boolean
)
