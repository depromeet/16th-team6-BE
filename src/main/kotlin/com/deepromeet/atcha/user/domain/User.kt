package com.deepromeet.atcha.user.domain

import com.deepromeet.atcha.location.domain.Coordinate

data class User(
    val id: UserId,
    val providerId: String,
    val homeAddress: HomeAddress,
    val fcmToken: String?,
    val isDeleted: Boolean = false
) {
    fun getHomeCoordinate(): Coordinate {
        return homeAddress.coordinate
    }

    fun updateHomeAddress(newAddress: HomeAddress): User {
        return copy(homeAddress = newAddress)
    }

    fun updateFcmToken(newToken: String?): User {
        return copy(fcmToken = newToken)
    }

    fun markAsDeleted(): User {
        return copy(isDeleted = true)
    }

    fun update(updateInfo: UserUpdateInfo): User {
        var updated = this

        updateInfo.getHomeAddress()?.let { newAddress ->
            updated = updated.updateHomeAddress(newAddress)
        }

        updateInfo.fcmToken?.let {
            updated = updated.updateFcmToken(it)
        }

        return updated
    }

    companion object {
        const val GUEST_PROVIDER_PREFIX = "GUEST:"

        fun guestProviderId(deviceId: String): String = GUEST_PROVIDER_PREFIX + deviceId

        fun createGuest(
            deviceId: String,
            fcmToken: String?
        ): User {
            require(deviceId.isNotBlank()) { "Device ID cannot be blank" }

            return User(
                id = UserId(0L),
                providerId = guestProviderId(deviceId),
                homeAddress = HomeAddress("", Coordinate(0.0, 0.0)),
                fcmToken = fcmToken,
                isDeleted = false
            )
        }

        fun create(
            providerId: String,
            homeAddress: HomeAddress,
            fcmToken: String
        ): User {
            require(providerId.isNotBlank()) { "Provider ID cannot be blank" }

            return User(
                id = UserId(0L),
                providerId = providerId,
                homeAddress = homeAddress,
                fcmToken = fcmToken,
                isDeleted = false
            )
        }
    }
}
