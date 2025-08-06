package com.deepromeet.atcha.user.application

import com.deepromeet.atcha.user.domain.HomeAddress
import com.deepromeet.atcha.user.domain.User
import com.deepromeet.atcha.user.domain.UserId
import com.deepromeet.atcha.user.domain.UserUpdateInfo
import com.deepromeet.atcha.user.domain.UserWithdrawalReason
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userReader: UserReader,
    private val userAppender: UserAppender,
    private val userUpdater: UserUpdater,
    private val userWithdrawalManager: UserWithdrawalManager
) {
    @Transactional(readOnly = true)
    fun getUser(id: UserId): User = userReader.read(id)

    @Transactional
    fun updateUser(
        id: UserId,
        userUpdateInfo: UserUpdateInfo
    ): User {
        val user = userReader.read(id)
        return userUpdater.update(user, userUpdateInfo)
    }

    @Transactional
    fun updateAlertFrequency(
        id: UserId,
        alertFrequency: MutableSet<Int>
    ): User {
        val user = userReader.read(id)
        return userUpdater.updateAlertFrequency(user, alertFrequency)
    }

    @Transactional
    fun updateHomeAddress(
        id: UserId,
        homeAddress: HomeAddress
    ): User {
        val user = userReader.read(id)
        return userUpdater.updateHomeAddress(user, homeAddress)
    }

    @Transactional
    fun deleteUser(reason: UserWithdrawalReason) {
        val user = userReader.read(reason.userId)
        userWithdrawalManager.withdraw(user, reason)
    }
}
