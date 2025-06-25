package com.deepromeet.atcha.user.api.response

import com.deepromeet.atcha.user.domain.User

data class UserInfoUpdateResponse(
    val id: Long,
    val providerId: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val alertFrequencies: MutableSet<Int>
) {
    companion object {
        fun from(domain: User) =
            UserInfoUpdateResponse(
                domain.id,
                domain.providerId,
                domain.address.address,
                domain.address.lat,
                domain.address.lon,
                domain.alertFrequencies
            )
    }
}
