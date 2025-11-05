package com.deepromeet.atcha.user.api.request

import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.domain.UserWithdrawalReason

data class UserWithdrawalRequest(
    val reason: String
) {
    fun toDomain(userId: UserId): UserWithdrawalReason {
        return UserWithdrawalReason(
            userId,
            reason
        )
    }
}
