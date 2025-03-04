package com.deepromeet.atcha.user.api.response

import com.deepromeet.atcha.user.domain.User

data class UserInfoResponse(
    val id: Long,
    val providerId: Long,
    val nickname: String,
    val profileImageUrl: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertAgreement: Boolean,
    val trackingAgreement: Boolean,
    val alertFrequencies: Set<Int>
) {
    companion object {
        fun from(domain: User) =
            UserInfoResponse(
                domain.id,
                domain.providerId,
                domain.nickname,
                domain.profileImageUrl,
                domain.address.address,
                domain.address.lat,
                domain.address.lon,
                domain.agreement.alert,
                domain.agreement.tracking,
                domain.alertFrequencies
            )
    }
}
