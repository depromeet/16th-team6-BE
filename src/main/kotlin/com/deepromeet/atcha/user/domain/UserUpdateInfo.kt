package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.location.domain.Coordinate

data class UserUpdateInfo(
    val nickname: String?,
    val profileImageUrl: String?,
    val address: String?,
    val lat: Double?,
    val lon: Double?,
    val fcmToken: String?
) {
    fun hasHomeAddress(): Boolean {
        return address != null && lat != null && lon != null
    }

    fun getHomeAddress(): HomeAddress? {
        return if (hasHomeAddress()) {
            HomeAddress(
                address = address!!,
                coordinate = Coordinate(lat = lat!!, lon = lon!!)
            )
        } else {
            null
        }
    }
}
