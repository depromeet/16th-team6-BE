package com.deepromeet.atcha.user.domain

data class UserUpdateInfo(
    val nickname: String?,
    val profileImageUrl: String?,
    val address: String?,
    val lat: Double?,
    val log: Double?,
    val alertFrequencies: Set<Int>?
)
