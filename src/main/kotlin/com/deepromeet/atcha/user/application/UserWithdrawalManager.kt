package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserWithdrawalReason
import org.springframework.stereotype.Component

@Component
class UserWithdrawalManager(
    private val userRepository: UserRepository,
) {

    fun withdraw(user: User, reason: UserWithdrawalReason) {
        val deletedUser = user.markAsDeleted()
        userRepository.saveWithdrawalReason(reason)
        userRepository.save(deletedUser)
    }
}
