package com.deepromeet.seulseul.auth.domain

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String
)
