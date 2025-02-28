package com.deepromeet.atcha.auth.infrastructure.response


data class ProviderUserInfoResponse(
    val providerId: Long,
    val nickname: String,
    val profileImageUrl: String,
)
