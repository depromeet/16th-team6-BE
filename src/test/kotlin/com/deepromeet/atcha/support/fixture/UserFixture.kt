package com.deepromeet.atcha.support.fixture

import com.deepromeet.atcha.auth.api.request.SignUpRequest
import com.deepromeet.atcha.user.domain.Address
import com.deepromeet.atcha.user.domain.User

object UserFixture {
    fun create(
        providerId: Long = 1L,
        nickname: String = "TEST_USER",
        profileImageUrl: String = "TEST_PROFILE_URL",
        address: Address =
            Address(
                "TEST_ADDRESS",
                37.0,
                127.0
            ),
        alertFrequencies: MutableSet<Int> = mutableSetOf(1, 5, 10),
        fcmToken: String = "TEST_FCMTOKEN"
    ): User =
        User(
            providerId = providerId,
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            address = address,
            alertFrequencies = alertFrequencies,
            fcmToken = fcmToken
        )

    fun userToSignUpRequest(
        user: User,
        provider: Int
    ): SignUpRequest =
        SignUpRequest(
            provider = provider,
            address = user.address.address,
            lat = user.address.lat,
            lon = user.address.lon,
            alertFrequencies = user.alertFrequencies,
            fcmToken = user.fcmToken
        )
}
