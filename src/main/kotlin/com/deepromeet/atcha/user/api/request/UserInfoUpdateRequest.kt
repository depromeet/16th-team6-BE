package com.deepromeet.atcha.user.api.request

data class UserInfoUpdateRequest(
    val nickname: String,
    val thumbnailImageUrl: String,
    val profileImageUrl: String,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean,
)
