package com.deepromeet.atcha.user.api.request

import com.deepromeet.atcha.user.domain.UserUpdateInfo

data class UserInfoUpdateRequest(
    val nickname: String?,
    val thumbnailImageUrl: String?,
    val profileImageUrl: String?,
    val address: String?,
    val lat: Double?,
    val log: Double?,
    val alertAgreement: Boolean?,
    val trackingAgreement: Boolean?
) {
    fun toUpdateUserInfo(): UserUpdateInfo {
        return UserUpdateInfo(
            nickname = nickname,
            thumbnailImageUrl = thumbnailImageUrl,
            profileImageUrl = profileImageUrl,
            address = address,
            lat = lat,
            log = log,
            alertAgreement = alertAgreement,
            trackingAgreement = trackingAgreement
        )
    }
}

data class AgreementRequest(
    val alert: Boolean = true,
    val tracking: Boolean = true
)
