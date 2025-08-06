package com.deepromeet.atcha.user.domain

data class UserWithdrawalReason(
    val userId: UserId,
    val reason: String
)
