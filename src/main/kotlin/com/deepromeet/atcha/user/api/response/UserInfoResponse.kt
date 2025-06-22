package com.deepromeet.atcha.user.api.response

import com.deepromeet.atcha.app.domain.AppVersion
import com.deepromeet.atcha.user.domain.User

data class UserInfoResponse(
    val id: Long,
    val providerId: String,
    val nickname: String,
    val profileImageUrl: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertFrequencies: MutableSet<Int>,
    val appVersion: String
) {
    companion object {
        fun from(
            domain: User,
            appVersion: AppVersion
        ) = UserInfoResponse(
            domain.id,
            domain.providerId,
            domain.nickname,
            domain.profileImageUrl,
            domain.address.address,
            domain.address.lat,
            domain.address.lon,
            domain.alertFrequencies,
            appVersion = appVersion.version
        )
    }
}
