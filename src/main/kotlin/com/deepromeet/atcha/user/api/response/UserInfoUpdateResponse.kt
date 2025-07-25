package com.deepromeet.atcha.user.api.response

import com.deepromeet.atcha.user.domain.User

data class UserInfoUpdateResponse(
    val id: Long,
    val providerId: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertFrequencies: Set<Int>
) {
    companion object {
        fun from(domain: User) =
            UserInfoUpdateResponse(
                domain.id.value,
                domain.providerId,
                domain.homeAddress?.address ?: "",
                domain.homeAddress?.latitude ?: 0.0,
                domain.homeAddress?.longitude ?: 0.0,
                domain.alertFrequencies
            )
    }
}
