package com.deepromeet.atcha.user.api.request

data class UserInfoUpdateRequest(
    val nickname: String,
    val thumbnailImageUrl: String,
    val profileImageUrl: String,
    val agreement: AgreementRequest
)

data class AgreementRequest(
    val alert: Boolean = true,
    val tracking: Boolean = true
)
