package com.deepromeet.atcha.auth.api.request

data class GuestAuthRequest(
    val deviceId: String,
    val fcmToken: String? = null
)
