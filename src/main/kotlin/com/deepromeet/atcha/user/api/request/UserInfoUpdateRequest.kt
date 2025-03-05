package com.deepromeet.atcha.user.api.request

import com.deepromeet.atcha.user.domain.UserUpdateInfo

data class UserInfoUpdateRequest(
    val nickname: String?,
    val profileImageUrl: String?,
    val address: String?,
    val lat: Double?,
    val log: Double?,
    val alertFrequencies: Set<Int>?
) {
    fun toUpdateUserInfo(): UserUpdateInfo {
        return UserUpdateInfo(
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            address = address,
            lat = lat,
            log = log,
            alertFrequencies = alertFrequencies
        )
    }
}
