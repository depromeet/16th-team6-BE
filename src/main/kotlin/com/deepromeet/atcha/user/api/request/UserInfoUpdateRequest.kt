package com.deepromeet.atcha.user.api.request

import com.deepromeet.atcha.user.domain.UserUpdateInfo

data class UserInfoUpdateRequest(
    val nickname: String? = null,
    val profileImageUrl: String? = null,
    val address: String? = null,
    val lat: Double? = null,
    val log: Double? = null,
    val alertFrequencies: Set<Int>? = null,
    val fcmToken: String? = null
) {
    fun toUpdateUserInfo(): UserUpdateInfo {
        return UserUpdateInfo(
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            address = address,
            lat = lat,
            log = log,
            alertFrequencies = alertFrequencies,
            fcmToken = fcmToken
        )
    }
}
