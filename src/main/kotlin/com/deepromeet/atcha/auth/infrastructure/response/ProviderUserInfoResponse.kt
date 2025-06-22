package com.deepromeet.atcha.auth.infrastructure.response

data class ProviderUserInfoResponse(
    val providerId: String,
    val profileImageUrl: String,
    val nickname: String? = null
)
