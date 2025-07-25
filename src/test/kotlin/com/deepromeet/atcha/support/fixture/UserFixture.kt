package com.deepromeet.atcha.support.fixture

import com.deepromeet.atcha.auth.api.request.SignUpRequest
import com.deepromeet.atcha.user.domain.HomeAddress
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId

object UserFixture {
    fun create(
        id: Long = 1L,
        providerId: String = "1",
        homeAddress: HomeAddress? =
            HomeAddress(
                address = "TEST_ADDRESS",
                latitude = 37.0,
                longitude = 127.0
            ),
        alertFrequencies: Set<Int> = setOf(1, 5, 10),
        fcmToken: String? = "TEST_FCMTOKEN"
    ): User =
        User(
            id = UserId(id),
            providerId = providerId,
            homeAddress = homeAddress,
            alertFrequencies = alertFrequencies,
            fcmToken = fcmToken
        )

    fun userToSignUpRequest(
        user: User,
        provider: Int
    ): SignUpRequest =
        SignUpRequest(
            provider = provider,
            address = user.homeAddress?.address ?: "",
            lat = user.homeAddress?.latitude ?: 0.0,
            lon = user.homeAddress?.longitude ?: 0.0,
            alertFrequencies = user.alertFrequencies.toMutableSet(),
            fcmToken = user.fcmToken ?: ""
        )
}
