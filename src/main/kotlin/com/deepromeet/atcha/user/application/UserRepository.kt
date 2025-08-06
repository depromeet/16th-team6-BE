package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.domain.UserWithdrawalReason

interface UserRepository {
    fun findById(userId: UserId): User?

    fun findByProviderId(providerId: String): User?

    fun existsByProviderId(providerId: String): Boolean

    fun save(user: User): User

    fun delete(userId: UserId)

    fun saveWithdrawalReason(reason: UserWithdrawalReason)
}
